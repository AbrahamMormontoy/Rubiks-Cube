package rubikscube;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


// PDB erforms a Breadth-First Search (BFS) to a depth of 4 to cache common start/end states, which speeds up the solver significantly
public class PDB {
    private static Map<String, PDBEntry> database = null;

    // Valid moves
    private static final String[] POSSIBLE_MOVES = {
            "F", "B", "L", "R", "U", "D",       // Standard 90 degree turns
            "FF", "BB", "LL", "RR", "UU", "DD", // Double turns 180 degrees
            "FFF", "BBB", "LLL", "RRR", "UUU", "DDD" // Inverse/270 turns
    };

    public static void initialize() {
        if (database != null) {
            return;
        }

        System.out.println("PDB: Depth 4");
        long startTime = System.currentTimeMillis();

        database = new HashMap<>();
        
        // Start from a clean, solved cube
        RubiksCube startNode = new RubiksCube();
        String startKey = startNode.toString();

        // Queue for BFS
        Queue<SearchNode> processingQueue = new LinkedList<>();
        
        // Seed the queue
        processingQueue.add(new SearchNode(startNode, 0, ""));
        database.put(startKey, new PDBEntry(0, ""));

        final int DEPTH_LIMIT = 4;

        while (!processingQueue.isEmpty()) {
            SearchNode current = processingQueue.poll();

            // Stop branching if we hit the depth limit
            if (current.depth >= DEPTH_LIMIT) {
                continue;
            }

            // Try branching out with every possible move
            for (String moveOp : POSSIBLE_MOVES) {
                
                // Optimization: Don't make silly moves (like twisting the same face 4 times)
                if (isRedundantMove(current.history, moveOp)) {
                    continue;
                }

                // Create the new state
                RubiksCube nextCube = current.cubeInstance.deepClone();
                nextCube.applyMoves(moveOp);
                
                String stateKey = nextCube.toString();
                
                // Build the path string (e.g., "F|R|U")
                String nextPath;
                if (current.history.isEmpty()) {
                    nextPath = moveOp;
                } else {
                    nextPath = current.history + "|" + moveOp;
                }

                // If we haven't seen this state, save it and add to queue
                if (!database.containsKey(stateKey)) {
                    PDBEntry entry = new PDBEntry(current.depth + 1, nextPath);
                    database.put(stateKey, entry);
                    
                    processingQueue.add(new SearchNode(nextCube, current.depth + 1, nextPath));
                }
            }
        }

        double duration = (System.currentTimeMillis() - startTime) / 1000.0;
        System.out.println("PDB ready. States: " + database.size() + " (Time: " + duration + "s)");
    }

    /**
     * Checks the database for a specific cube state.
     * @return The entry with depth/path, or null if not found.
     */
    public static PDBEntry lookup(RubiksCube cube) {
        if (database == null) {
            return null;
        }
        return database.get(cube.toString());
    }

    // Helper to just get the depth integer
    public static int getDepth(RubiksCube cube) {
        PDBEntry found = lookup(cube);
        if (found != null) {
            return found.depth;
        }
        return -1;
    }

    // Helper to just get the solution path string
    public static String getPath(RubiksCube cube) {
        PDBEntry found = lookup(cube);
        if (found != null) {
            return found.path;
        }
        return null;
    }

    // F followed by FFF is an example
    private static boolean isRedundantMove(String pathSoFar, String proposedMove) {
        if (pathSoFar == null || pathSoFar.isEmpty()) {
            return false;
        }

        String lastOp = pathSoFar;
        int lastPipeIndex = pathSoFar.lastIndexOf('|');
        if (lastPipeIndex >= 0) {
            lastOp = pathSoFar.substring(lastPipeIndex + 1);
        }

        char nextFace = proposedMove.charAt(0);
        char lastFace = lastOp.charAt(0);

        if (lastFace == nextFace) {
            int previousTurns = lastOp.length(); 
            int proposedTurns = proposedMove.length();
            
            if (previousTurns + proposedTurns >= 4) {
                return true;
            }
        }

        if (areFacesOpposite(lastFace, nextFace)) {
            int secondLastPipe = pathSoFar.lastIndexOf('|', lastPipeIndex - 1);
            
            if (secondLastPipe >= 0) {
                String secondToLastOp = pathSoFar.substring(secondLastPipe + 1, lastPipeIndex);
                char secondLastFace = secondToLastOp.charAt(0);
                
                if (secondLastFace == nextFace) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean areFacesOpposite(char a, char b) {
        boolean fb = (a == 'F' && b == 'B') || (a == 'B' && b == 'F');
        boolean lr = (a == 'L' && b == 'R') || (a == 'R' && b == 'L');
        boolean ud = (a == 'U' && b == 'D') || (a == 'D' && b == 'U');
        
        return fb || lr || ud;
    }


    public static void save(String filePath) throws IOException {
        if (database == null) return;
        
        try (FileOutputStream fos = new FileOutputStream(filePath);
             ObjectOutputStream out = new ObjectOutputStream(fos)) {
            
            out.writeObject(database);
            System.out.println("PDB saved to disk: " + filePath);
        }
    }

    @SuppressWarnings("unchecked")
    public static void load(String filePath) throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(filePath);
             ObjectInputStream in = new ObjectInputStream(fis)) {
            
            database = (Map<String, PDBEntry>) in.readObject();
            System.out.println("PDB loaded from disk. Size: " + database.size());
        }
    }

    // Helper functions
    public static class PDBEntry implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public final int depth;
        public final String path;

        public PDBEntry(int depth, String path) {
            this.depth = depth;
            this.path = path;
        }
    }

    private static class SearchNode {
        RubiksCube cubeInstance;
        int depth;
        String history;

        SearchNode(RubiksCube cube, int depth, String history) {
            this.cubeInstance = cube;
            this.depth = depth;
            this.history = history;
        }
    }
}