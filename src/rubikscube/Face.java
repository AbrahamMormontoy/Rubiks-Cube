package rubikscube;

import java.util.HashMap;
import java.util.Map;

enum FaceType {
    F, B, R, L, U, D
}

// Helper class to support the rotation of the faces
// To make the rotation logic simpler it uses a temporary 7x3 grid
public class Face {
    final Map<FaceType, String[]> COORDINATE_MAPPING = new HashMap<FaceType, String[]>() {
        {
            // Front Face
            put(FaceType.F, new String[] { 
                "3_3", "3_4", "3_5", "4_3", "4_4", "4_5", "5_3", "5_4", "5_5",
                "2_3", "2_4", "2_5",
                "3_6", "4_6", "5_6",
                "6_5", "6_4", "6_3",
                "5_2", "4_2", "3_2"
            });
            // Back Face
            put(FaceType.B, new String[] { 
                "3_9", "3_10", "3_11", "4_9", "4_10", "4_11", "5_9", "5_10", "5_11", 
                "0_5", "0_4", "0_3", 
                "3_0", "4_0", "5_0", 
                "8_3", "8_4", "8_5", 
                "5_8", "4_8", "3_8" 
            });
            // Right Face
            put(FaceType.R, new String[] { 
                "3_6", "3_7", "3_8", "4_6", "4_7", "4_8", "5_6", "5_7", "5_8", 
                "2_5", "1_5", "0_5", 
                "3_9", "4_9", "5_9", 
                "8_5", "7_5", "6_5", 
                "5_5", "4_5", "3_5" 
            });
            // Left Face
            put(FaceType.L, new String[] { 
                "3_0", "3_1", "3_2", "4_0", "4_1", "4_2", "5_0", "5_1", "5_2", 
                "0_3", "1_3", "2_3", 
                "3_3", "4_3", "5_3", 
                "6_3", "7_3", "8_3", 
                "5_11", "4_11", "3_11" 
            });
            // Up Face
            put(FaceType.U, new String[] { 
                "0_3", "0_4", "0_5", "1_3", "1_4", "1_5", "2_3", "2_4", "2_5", 
                "3_11", "3_10", "3_9", 
                "3_8", "3_7", "3_6", 
                "3_5", "3_4", "3_3", 
                "3_2", "3_1", "3_0" 
            });
            // Down Face
            put(FaceType.D, new String[] { 
                "6_3", "6_4", "6_5", "7_3", "7_4", "7_5", "8_3", "8_4", "8_5", 
                "5_3", "5_4", "5_5", 
                "5_6", "5_7", "5_8", 
                "5_9", "5_10", "5_11", 
                "5_0", "5_1", "5_2" 
            });
        }
    };

    // Store face data
    String[][] localGrid;
    FaceType currentType;

    Face(String[][] mainCube, FaceType type) {
        this.localGrid = new String[7][3];
        this.currentType = type;
        
        String[] mapping = COORDINATE_MAPPING.get(type);
        int mapIndex = 0;
        
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 3; col++) {
                String rawCoord = mapping[mapIndex];
                String[] parts = rawCoord.split("_");
                
                int targetRow = Integer.parseInt(parts[0]);
                int targetCol = Integer.parseInt(parts[1]);
                
                this.localGrid[row][col] = mainCube[targetRow][targetCol];
                mapIndex++;
            }
        }
    }

    // Performs the rotation of the face
    public void rotateFace() {
        String[][] snapshot = new String[7][3];
        
        // Backup current state
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 3; c++) {
                snapshot[r][c] = this.localGrid[r][c];
            }
        }
        
        this.localGrid = new String[7][3];
        
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 3; c++) {
                
                if (r < 3) { 
                    this.localGrid[c][2 - r] = snapshot[r][c];
                } else { 
                    int nextRow = r + 1;
                    if (nextRow > 6) {
                        nextRow = 3;
                    }
                    this.localGrid[nextRow][c] = snapshot[r][c];
                }
            }
        }
    }

    public void modifyCube(String[][] mainCube) {
        int mapIndex = 0;
        String[] mapping = COORDINATE_MAPPING.get(this.currentType);
        
        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 3; col++) {
                String rawCoord = mapping[mapIndex];
                mapIndex++;
                
                String[] parts = rawCoord.split("_");
                int targetRow = Integer.parseInt(parts[0]);
                int targetCol = Integer.parseInt(parts[1]);
                
                mainCube[targetRow][targetCol] = this.localGrid[row][col];
            }
        }
    }
}