package rubikscube;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

// Main solver class implementing the A* search algorithm.
public class Solver {

    // Define moves as a constant for easy access
    private static final String[] VALID_MOVES = { 
        "F", "B", "L", "R", "U", "D", 
        "FF", "BB", "LL", "RR", "UU", "DD", 
        "FFF", "LLL", "RRR", "UUU", "DDD", "BBB" 
    };

    static class SearchNode {
        RubiksCube state;
        String history;
        int movesTaken;
        int estimatedLeft;

        public SearchNode(RubiksCube cube, String history) {
            this.state = cube;
            this.history = history;
            
            this.movesTaken = calculateDepth(history);

            this.estimatedLeft = HeuristicEstimation.estimate(cube);
        }

        // f(n) = g(n) + h(n)
        public int getTotalScore() {
            return movesTaken + estimatedLeft;
        }

        // Helper to count moves by splitting the string
        private int calculateDepth(String path) {
            if (path == null || path.isEmpty()) {
                return 0;
            }
            return path.substring(1).split("\\|").length;
        }
    }

    public static void main(String[] args) {
        // Validate input arguments
        if (args.length < 2) {
            System.err.println("Error: Missing input/output files.");
            System.out.println("Usage: java " + MethodHandles.lookup().lookupClass().getName() + " [input_file] [output_file]");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        // PDB
        long initStart = System.currentTimeMillis();
        PDB.initialize();
        System.out.println("----------------------------------------------------------------------------------------------");

        // Search
        Map<String, Integer> closedSet = new HashMap<>();
        
        PriorityQueue<SearchNode> frontier = new PriorityQueue<>(
            Comparator.comparingInt(SearchNode::getTotalScore)
        );

        int totalIterations = 0;

        try {
            // Initial cube state
            RubiksCube startCube = new RubiksCube(inputFile);
            SearchNode root = new SearchNode(startCube, "");
            
            frontier.add(root);
            closedSet.put(startCube.toString(), 0);

            // Start the A* Loop
            System.out.println("A* search for " + outputFile + ":");
            
            while (!frontier.isEmpty()) {
                totalIterations++;

                if (totalIterations > 7000) {
                    System.out.println("Solver: Hit step limit (7000). Aborting.");
                    break;
                }

                // Get the best candidate
                SearchNode current = frontier.poll();
                String currentStr = current.state.toString();

                // Closed Set Optimization:
                // If we found a faster way to this state previously, skip this path.
                if (closedSet.containsKey(currentStr) && closedSet.get(currentStr) < current.movesTaken) {
                    continue;
                }

                // Periodic logging
                if (totalIterations % 100 == 0) {
                    printStatusLog(totalIterations, frontier.size(), current);
                }

                // Check 1: Is this state in our Pattern Database?
                PDB.PDBEntry dbHit = PDB.lookup(current.state);
                if (dbHit != null) {
                    // Found it! 
                    // FIX: The PDB path generates the state FROM solved. 
                    // To solve the cube, we must apply the INVERSE of that path.
                    String invertedPath = invertPath(dbHit.path);
                    String finalPath = mergePathWithDatabase(current.history, invertedPath);
                    printSuccess(finalPath, totalIterations, initStart, outputFile);
                    break;
                }

                // Check 2: Is it solved normally?
                if (current.state.isSolved()) {
                    printSuccess(current.history, totalIterations, initStart, outputFile);
                    break;
                }

                // Mark current as visited
                closedSet.put(currentStr, current.movesTaken);

                // Expand neighbors: Try every possible move
                for (String move : VALID_MOVES) {
                    
                    // Pruning: Check if this move is redundant (e.g., F followed by FFF)
                    if (isRedundantMove(current.history, move)) {
                        continue;
                    }

                    // Pruning: Check for complex redundant patterns (mostly opposite face interactions)
                    if (isComplexRedundant(current.history, move)) {
                        continue;
                    }

                    // Apply the move
                    RubiksCube nextCube = current.state.deepClone();
                    nextCube.applyMoves(move);
                    
                    String nextStr = nextCube.toString();
                    int nextCost = current.movesTaken + 1;

                    // If we haven't been here before (or we found a shorter path), add to frontier
                    if (!closedSet.containsKey(nextStr) || closedSet.get(nextStr) > nextCost) {
                        closedSet.put(nextStr, nextCost);
                        
                        String newHistory = current.history + "|" + move;
                        frontier.add(new SearchNode(nextCube, newHistory));
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error: Could not read input file.");
        } catch (Exception e) { // Catching generic FormatException or others
            System.err.println("Error: Invalid file format.");
            e.printStackTrace();
        }
    }

    // Helper important function

    // Fix the moves after PDB implementation
    private static String invertPath(String path) {
        if (path == null || path.isEmpty()) return "";
        String[] moves = path.split("\\|");
        StringBuilder sb = new StringBuilder();
        
        for (int i = moves.length - 1; i >= 0; i--) {
            sb.append(invertMove(moves[i]));
            if (i > 0) {
                sb.append("|");
            }
        }
        return sb.toString();
    }

    private static String invertMove(String move) {
        if (move.length() == 1) {
            return move + move + move;
        }
        if (move.length() == 3) {
            return move.substring(0, 1);
        }
        return move;
    }

    private static boolean isRedundantMove(String history, String nextMove) {
        String lastMove = getLastMove(history);
        
        if (lastMove != null && !lastMove.isEmpty()) {
            String combined = lastMove + nextMove;
            
            if (combined.length() >= 4) {
                char first = combined.charAt(0);
                boolean allMatch = true;
                
                int startCheck = combined.length() - 4;
                for (int i = 0; i < 4; i++) {
                    if (combined.charAt(startCheck + i) != first) {
                        allMatch = false;
                        break;
                    }
                }
                
                if (allMatch) return true;
            }
        }
        return false;
    }

    private static boolean isComplexRedundant(String history, String nextMove) {
        String lastMove = getLastMove(history);
        
        if (lastMove != null && !lastMove.isEmpty()) {
            char currentFace = nextMove.charAt(0);
            
            int lastDiffIndex = -1;
            for (int j = lastMove.length() - 1; j >= 0; j--) {
                if (lastMove.charAt(j) != lastMove.charAt(lastMove.length() - 1)) {
                    lastDiffIndex = j;
                    break;
                }
            }

            if (lastDiffIndex >= 0) {
                char prevFace = lastMove.charAt(lastDiffIndex);
                char endFace = lastMove.charAt(lastMove.length() - 1);
                
                if (prevFace == currentFace && areFacesOpposite(prevFace, endFace)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getLastMove(String history) {
        if (history != null && !history.isEmpty()) {
            String[] parts = history.split("\\|");
            return parts[parts.length - 1];
        }
        return "";
    }

    private static boolean areFacesOpposite(char f1, char f2) {
        return (f1 == 'F' && f2 == 'B') || (f1 == 'B' && f2 == 'F') ||
               (f1 == 'L' && f2 == 'R') || (f1 == 'R' && f2 == 'L') ||
               (f1 == 'U' && f2 == 'D') || (f1 == 'D' && f2 == 'U');
    }

    private static String mergePathWithDatabase(String currentPath, String dbPath) {
        if (dbPath.isEmpty()) {
            return currentPath;
        }
        
        StringBuilder sb = new StringBuilder(currentPath);
        String[] dbMoves = dbPath.split("\\|");
        
        for (String m : dbMoves) {
            if (!m.isEmpty()) {
                sb.append("|").append(m);
            }
        }
        return sb.toString();
    }

    private static void printStatusLog(int steps, int queueSize, SearchNode node) {
        System.out.println("Steps: " + steps + " | Queue: " + queueSize);
        System.out.println("   -> Path: " + node.history);
        System.out.println("   -> Cost (g/h/f): " + node.movesTaken + " / " + node.estimatedLeft + " / " + node.getTotalScore());
    }

    private static void printSuccess(String rawSolution, int steps, long startTime, String outputFilePath) {
    String cleanSolution = rawSolution.replaceAll("\\|", "");
    long duration = System.currentTimeMillis() - startTime;
    
    System.out.println("Steps " + steps);
    System.out.println("Solution: " + cleanSolution);
    System.out.println("Time: " + duration + " ms");

    try (java.io.PrintWriter out = new java.io.PrintWriter(outputFilePath)) {
        out.write(cleanSolution);
        System.out.println("Output in: " + outputFilePath);
    } catch (IOException e) {
        System.err.println("Error: " + e.getMessage());
    }
}
}