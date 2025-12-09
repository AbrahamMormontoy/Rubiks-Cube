package rubikscube;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


// Calculates heuristic estimation for A* search.
// Uses a combination of PDB lookups and Manhattan-distance style estimation for corners and edges.
public class HeuristicEstimation {

    // Main entry point for getting the heuristic value
    public static int estimate(RubiksCube cubeInstance) {
        if (cubeInstance.isSolved()) {
            return 0;
        }

        int dbValue = getDatabasePrediction(cubeInstance);
        if (dbValue != -1) {
            return dbValue;
        }

        // If not in DB, calculate it
        return calculateComplexHeuristic(cubeInstance);
    }

    // Helper function to check the Pattern Database
    private static int getDatabasePrediction(RubiksCube cube) {
        PDB.PDBEntry entry = PDB.lookup(cube);
        
        if (entry != null) {
            return entry.depth;
        }
        return -1;
    }

    public static int calculateComplexHeuristic(RubiksCube cube) {
        if (cube.isSolved()) {
            return 0;
        }

        int patternScore = analyzePatterns(cube);
        if (patternScore >= 0) {
            return patternScore;
        }

        int hCorners = calculateCornerHeuristic(cube);
        int hEdges = calculateEdgeHeuristic(cube);

        int totalEstimate = Math.max(hCorners, hEdges);

        int minVal = Math.min(hCorners, hEdges);
        
        if (hCorners >= 8 && hEdges >= 8) {
            totalEstimate += (minVal / 3);
        } else if (hCorners >= 5 && hEdges >= 5) {
            totalEstimate += (minVal / 4);
        }

        return totalEstimate;
    }

    // The heuristic function 
    public static int estimateSimple(RubiksCube cube) {
        if (cube.isSolved()) return 0;

        int cSteps = getSimpleCornerDist(cube);
        int eSteps = getSimpleEdgeDist(cube);

        return (cSteps + eSteps) / 8;
    }

    // Corner functions

    private static int calculateCornerHeuristic(RubiksCube cube) {
        String[][] grid = cube.cube;
        int heuristicScore = 0;
        int wrongPosition = 0;
        int wrongOrientation = 0;

        // Define target states for all 8 corners
        CornerConfig[] targets = {
                new CornerConfig(new int[][] { { 2, 3 }, { 3, 2 }, { 3, 3 } }, new String[] { "O", "G", "W" }),
                new CornerConfig(new int[][] { { 2, 5 }, { 3, 5 }, { 3, 6 } }, new String[] { "O", "W", "B" }),
                new CornerConfig(new int[][] { { 0, 3 }, { 3, 11 }, { 3, 0 } }, new String[] { "O", "Y", "G" }),
                new CornerConfig(new int[][] { { 0, 5 }, { 3, 8 }, { 3, 9 } }, new String[] { "O", "B", "Y" }),
                new CornerConfig(new int[][] { { 6, 3 }, { 5, 2 }, { 5, 3 } }, new String[] { "R", "G", "W" }),
                new CornerConfig(new int[][] { { 6, 5 }, { 5, 5 }, { 5, 6 } }, new String[] { "R", "W", "B" }),
                new CornerConfig(new int[][] { { 8, 3 }, { 5, 11 }, { 5, 0 } }, new String[] { "R", "Y", "G" }),
                new CornerConfig(new int[][] { { 8, 5 }, { 5, 8 }, { 5, 9 } }, new String[] { "R", "B", "Y" })
        };

        for (CornerConfig corner : targets) {
            int val = scoreCorner(grid, corner);
            heuristicScore += val;

            if (val >= 4) {
                wrongPosition++;
            } else if (val > 0) {
                wrongOrientation++;
            }
        }

        int finalScore = heuristicScore / 4;

        if (wrongPosition > 0) {
            finalScore += wrongPosition;
        }

        if (wrongOrientation >= 4) {
            finalScore += 2;
        } else if (wrongOrientation >= 2) {
            finalScore += 1;
        }

        return finalScore;
    }

    private static int scoreCorner(String[][] grid, CornerConfig config) {
        String color1 = grid[config.coords[0][0]][config.coords[0][1]];
        String color2 = grid[config.coords[1][0]][config.coords[1][1]];
        String color3 = grid[config.coords[2][0]][config.coords[2][1]];

        Set<String> currentSet = new HashSet<>();
        currentSet.add(color1);
        currentSet.add(color2);
        currentSet.add(color3);

        Set<String> targetSet = new HashSet<>(Arrays.asList(config.expectedColors));

        if (!currentSet.equals(targetSet)) {
            return 2; 
        }

        boolean isAligned = color1.equals(config.expectedColors[0]) &&
                            color2.equals(config.expectedColors[1]) &&
                            color3.equals(config.expectedColors[2]);

        if (isAligned) {
            return 0; 
        }

        return 2; 
    }


    // Edge analysis

    private static int calculateEdgeHeuristic(RubiksCube cube) {
        String[][] grid = cube.cube;
        int totalScore = 0;
        int misplaced = 0;
        int flipped = 0;

        EdgeConfig[] edgeTargets = {
                new EdgeConfig(new int[][] { { 2, 4 }, { 3, 4 } }, new String[] { "O", "W" }),
                new EdgeConfig(new int[][] { { 1, 5 }, { 3, 7 } }, new String[] { "O", "B" }),
                new EdgeConfig(new int[][] { { 0, 4 }, { 3, 10 } }, new String[] { "O", "Y" }),
                new EdgeConfig(new int[][] { { 1, 3 }, { 3, 1 } }, new String[] { "O", "G" }),
                new EdgeConfig(new int[][] { { 4, 2 }, { 4, 3 } }, new String[] { "G", "W" }),
                new EdgeConfig(new int[][] { { 4, 5 }, { 4, 6 } }, new String[] { "W", "B" }),
                new EdgeConfig(new int[][] { { 4, 11 }, { 4, 0 } }, new String[] { "Y", "G" }),
                new EdgeConfig(new int[][] { { 4, 8 }, { 4, 9 } }, new String[] { "B", "Y" }),
                new EdgeConfig(new int[][] { { 5, 4 }, { 6, 4 } }, new String[] { "W", "R" }),
                new EdgeConfig(new int[][] { { 5, 7 }, { 7, 5 } }, new String[] { "B", "R" }),
                new EdgeConfig(new int[][] { { 5, 10 }, { 8, 4 } }, new String[] { "Y", "R" }),
                new EdgeConfig(new int[][] { { 5, 1 }, { 7, 3 } }, new String[] { "G", "R" })
        };

        for (EdgeConfig edge : edgeTargets) {
            int score = scoreEdge(grid, edge);
            totalScore += score;

            if (score >= 4) {
                misplaced++;
            } else if (score == 2) {
                flipped++;
            }
        }

        int finalScore = totalScore / 4;

        if (misplaced > 0) {
            finalScore += (misplaced / 2);
        }

        if (flipped >= 4) {
            finalScore += 2;
        } else if (flipped >= 2) {
            finalScore += 1;
        }

        return finalScore;
    }

    private static int scoreEdge(String[][] grid, EdgeConfig config) {
        String c1 = grid[config.coords[0][0]][config.coords[0][1]];
        String c2 = grid[config.coords[1][0]][config.coords[1][1]];

        Set<String> curSet = new HashSet<>();
        curSet.add(c1);
        curSet.add(c2);
        
        Set<String> targetSet = new HashSet<>(Arrays.asList(config.expectedColors));

        if (!curSet.equals(targetSet)) {
            return 2;
        }

        if (c1.equals(config.expectedColors[0]) && c2.equals(config.expectedColors[1])) {
            return 0; 
        }

        // Flipped?
        if (c1.equals(config.expectedColors[1]) && c2.equals(config.expectedColors[0])) {
            return 1;
        }

        return 3;
    }

    // Estimation functions

    private static int getSimpleCornerDist(RubiksCube cube) {
        int sum = 0;
        String[][] grid = cube.cube;

        SimpleCorner[] corners = {
                new SimpleCorner(new int[][] { { 2, 3 }, { 3, 2 }, { 3, 3 } }, new String[] { "O", "G", "W" }),
                new SimpleCorner(new int[][] { { 2, 5 }, { 3, 5 }, { 3, 6 } }, new String[] { "O", "W", "B" }),
                new SimpleCorner(new int[][] { { 0, 3 }, { 3, 11 }, { 3, 0 } }, new String[] { "O", "Y", "G" }),
                new SimpleCorner(new int[][] { { 0, 5 }, { 3, 8 }, { 3, 9 } }, new String[] { "O", "B", "Y" }),
                new SimpleCorner(new int[][] { { 5, 2 }, { 5, 3 }, { 6, 3 } }, new String[] { "G", "W", "R" }),
                new SimpleCorner(new int[][] { { 5, 5 }, { 5, 6 }, { 6, 5 } }, new String[] { "W", "B", "R" }),
                new SimpleCorner(new int[][] { { 5, 11 }, { 5, 0 }, { 8, 3 } }, new String[] { "Y", "G", "R" }),
                new SimpleCorner(new int[][] { { 5, 8 }, { 5, 9 }, { 8, 5 } }, new String[] { "B", "Y", "R" })
        };

        for (SimpleCorner c : corners) {
            sum += evalSimpleCorner(grid, c);
        }
        return sum;
    }

    private static int getSimpleEdgeDist(RubiksCube cube) {
        int sum = 0;
        String[][] grid = cube.cube;

        SimpleEdge[] edges = {
                new SimpleEdge(new int[][] { { 2, 4 }, { 3, 4 } }, new String[] { "O", "W" }),
                new SimpleEdge(new int[][] { { 1, 5 }, { 3, 7 } }, new String[] { "O", "B" }),
                new SimpleEdge(new int[][] { { 0, 4 }, { 3, 10 } }, new String[] { "O", "Y" }),
                new SimpleEdge(new int[][] { { 1, 3 }, { 3, 1 } }, new String[] { "O", "G" }),
                new SimpleEdge(new int[][] { { 4, 2 }, { 4, 3 } }, new String[] { "G", "W" }),
                new SimpleEdge(new int[][] { { 4, 5 }, { 4, 6 } }, new String[] { "W", "B" }),
                new SimpleEdge(new int[][] { { 4, 11 }, { 4, 0 } }, new String[] { "Y", "G" }),
                new SimpleEdge(new int[][] { { 4, 8 }, { 4, 9 } }, new String[] { "B", "Y" }),
                new SimpleEdge(new int[][] { { 5, 4 }, { 6, 4 } }, new String[] { "W", "R" }),
                new SimpleEdge(new int[][] { { 5, 7 }, { 7, 5 } }, new String[] { "B", "R" }),
                new SimpleEdge(new int[][] { { 5, 10 }, { 8, 4 } }, new String[] { "Y", "R" }),
                new SimpleEdge(new int[][] { { 5, 1 }, { 7, 3 } }, new String[] { "G", "R" })
        };

        for (SimpleEdge e : edges) {
            sum += evalSimpleEdge(grid, e);
        }
        return sum;
    }

    private static int evalSimpleCorner(String[][] grid, SimpleCorner target) {
        String[] foundColors = new String[3];
        
        for (int i = 0; i < 3; i++) {
            int r = target.pos[i][0];
            int c = target.pos[i][1];
            foundColors[i] = grid[r][c];
        }

        String[] sortedFound = Arrays.copyOf(foundColors, 3);
        String[] sortedTarget = Arrays.copyOf(target.colors, 3);
        Arrays.sort(sortedFound);
        Arrays.sort(sortedTarget);

        if (!Arrays.equals(sortedFound, sortedTarget)) {
            return 5;
        }

        if (Arrays.equals(foundColors, target.colors)) {
            return 0;
        }
        return 3;
    }

    private static int evalSimpleEdge(String[][] grid, SimpleEdge target) {
        String[] foundColors = new String[2];
        
        for (int i = 0; i < 2; i++) {
            int r = target.pos[i][0];
            int c = target.pos[i][1];
            foundColors[i] = grid[r][c];
        }

        String[] sortedFound = Arrays.copyOf(foundColors, 2);
        String[] sortedTarget = Arrays.copyOf(target.colors, 2);
        Arrays.sort(sortedFound);
        Arrays.sort(sortedTarget);

        if (!Arrays.equals(sortedFound, sortedTarget)) {
            return 4;
        }

        if (Arrays.equals(foundColors, target.colors)) {
            return 0;
        }
        return 3; 
    }

    // Analyze pattern

    private static int analyzePatterns(RubiksCube cube) {
        String[][] grid = cube.cube;

        int mismatches = 0;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 12; col++) {
                if (grid[row][col] == null) {
                    continue;
                }
                String expected = TargetColor(row, col);
                if (expected != null && !grid[row][col].equals(expected)) {
                    mismatches++;
                }
            }
        }

        // Exact match
        if (mismatches == 0) {
            return 0;
        } if (mismatches <= 3) { 
            return 1;
        } if (mismatches <= 6) { 
            return 2;
        } if (mismatches <= 9) { 
            return 3;
        }

        boolean isTopSolved = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 3; j < 6; j++) {
                if (!grid[i][j].equals("O")) {
                    isTopSolved = false;
                    break;
                }
            }
        }

        if (isTopSolved) {
            int bottomErrors = 0;
            for (int i = 6; i < 9; i++) {
                for (int j = 3; j < 6; j++) {
                    if (!grid[i][j].equals("R")) {
                        bottomErrors++;
                    }
                }
            }
            return Math.max(1, bottomErrors / 3);
        }

        return -1;
    }

    private static String TargetColor(int r, int c) {
        if (r >= 0 && r <= 2 && c >= 3 && c <= 5) {
            return "O"; // Top
        } if (r >= 3 && r <= 5 && c >= 3 && c <= 5) { 
            return "W"; // Front
        } if (r >= 6 && r <= 8 && c >= 3 && c <= 5) {
            return "R"; // Bottom
        } if (r >= 3 && r <= 5 && c >= 0 && c <= 2) { 
            return "G"; // Left
        } if (r >= 3 && r <= 5 && c >= 6 && c <= 8) { 
            return "B"; // Right
        } if (r >= 3 && r <= 5 && c >= 9 && c <= 11) { 
            return "Y"; // Back
        }
        return null;
    }

    // Helper functions

    static class SimpleEdge {
        int[][] pos;
        String[] colors;

        SimpleEdge(int[][] pos, String[] colors) {
            this.pos = pos;
            this.colors = colors;
        }
    }

    private static class EdgeConfig {
        int[][] coords;
        String[] expectedColors;

        EdgeConfig(int[][] coords, String[] expectedColors) {
            this.coords = coords;
            this.expectedColors = expectedColors;
        }
    }

    static class SimpleCorner {
        int[][] pos;
        String[] colors;

        SimpleCorner(int[][] pos, String[] colors) {
            this.pos = pos;
            this.colors = colors;
        }
    }

    private static class CornerConfig {
        int[][] coords;
        String[] expectedColors;

        CornerConfig(int[][] coords, String[] expectedColors) {
            this.coords = coords;
            this.expectedColors = expectedColors;
        }
    }
}