package rubikscube;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class RubiksCube {
    String[][] cube;
    Integer valideElementsCount = 0;

    final Integer TOTAL_ROWS = 9;
    final Integer TOTAL_COLUMNS = 12;
    final Integer TOTAL_ELEMENTS = 54;

    private String stateInit = 
            "   OOO\n" +
            "   OOO\n" +
            "   OOO\n" +
            "GGGWWWBBBYYY\n" +
            "GGGWWWBBBYYY\n" +
            "GGGWWWBBBYYY\n" +
            "   RRR\n" +
            "   RRR\n" +
            "   RRR\n";

    public static RubiksCube solvedCube = new RubiksCube();

    /**
     * default constructor
     * Creates a Rubik's Cube in an initial state:
     * OOO
     * OOO
     * OOO
     * GGGWWWBBBYYY
     * GGGWWWBBBYYY
     * GGGWWWBBBYYY
     * RRR
     * RRR
     * RRR
     */
    public RubiksCube() {
        this.cube = new String[9][12];
        // read file cube_init.txt
        for (int i = 0; i < TOTAL_ROWS; i++) {
            String line = this.stateInit.split("\n")[i];
            for (int j = 0; j < TOTAL_COLUMNS; j++) {
                // if the character is not exist continue to the next line
                if (j >= line.length()) {
                    continue;
                }
                cube[i][j] = String.valueOf(line.charAt(j));
                if (!cube[i][j].trim().isEmpty()) {
                    valideElementsCount++;
                }
            }
        }
    }

    /**
     * @param fileName
     * @throws IOException
     * @throws IncorrectFormatException
     *                                  Creates a Rubik's Cube from the description
     *                                  in fileName
     */
    public RubiksCube(String fileName) throws IOException, IncorrectFormatException {

        this.cube = new String[9][12];
        String[] filePaths = {
                fileName,
        };
        BufferedReader reader = null;
        for (String filePath : filePaths) {
            try {
                reader = new BufferedReader(new FileReader(filePath));
                // throw if file is not in correct format
                for (int i = 0; i < TOTAL_ROWS; i++) {
                    String line = reader.readLine();
                    for (int j = 0; j < TOTAL_COLUMNS; j++) {
                        if (j >= line.length()) {
                            continue;
                        }
                        cube[i][j] = String.valueOf(line.charAt(j));
                        if (!cube[i][j].trim().isEmpty()) {
                            valideElementsCount++;
                        }
                    }
                }
                reader.close();
                if (valideElementsCount != TOTAL_ELEMENTS) {
                    throw new IncorrectFormatException("Wrong format of the file");
                }
                break;
            } catch (IOException e) {
                // by pass the error try next path
            }
        }
        if (reader == null) {
            throw new IOException("Error reading file");
        }
    }

    public void resetCube(String cubeString) {
        // rest the cube with input form toString() output
        /*
         * current error :
         * Exception in thread "main" java.lang.StringIndexOutOfBoundsException: String
         * index out of range: 6
         * at java.base/java.lang.StringLatin1.charAt(StringLatin1.java:48)
         * at java.base/java.lang.String.charAt(String.java:1519)
         * at rubikscube.RubiksCube.resetCube(RubiksCube.java:109)
         * at rubikscube.Solver.main(Solver.java:61)
         */
        this.cube = new String[9][12];
        String[] lines = cubeString.split("\n");
        for (int i = 0; i < TOTAL_ROWS; i++) {
            String line = lines[i];
            for (int j = 0; j < TOTAL_COLUMNS; j++) {
                if (j >= line.length()) {
                    continue;
                }
                this.cube[i][j] = String.valueOf(line.charAt(j));
                if (!this.cube[i][j].trim().isEmpty()) {
                    valideElementsCount++;
                }
            }
        }
        valideElementsCount = 0;
        for (int i = 0; i < TOTAL_ROWS; i++) {
            for (int j = 0; j < TOTAL_COLUMNS; j++) {
                if (this.cube[i][j] != null && !this.cube[i][j].trim().isEmpty()) {
                    valideElementsCount++;
                }
            }
        }
    }

    private void applyMoveF() {
        Face face = new Face(this.cube, FaceType.F);
        face.rotateFace();
        face.modifyCube(this.cube);
    }

    private void applyMoveL() {
        Face face = new Face(this.cube, FaceType.L);
        face.rotateFace();
        face.modifyCube(this.cube);
    }

    private void applyMoveR() {
        Face face = new Face(this.cube, FaceType.R);
        face.rotateFace();
        face.modifyCube(this.cube);
    }

    private void applyMoveU() {
        Face face = new Face(this.cube, FaceType.U);
        face.rotateFace();
        face.modifyCube(this.cube);
    }

    private void applyMoveD() {
        Face face = new Face(this.cube, FaceType.D);
        face.rotateFace();
        face.modifyCube(this.cube);
    }

    private void applyMoveB() {
        Face face = new Face(this.cube, FaceType.B);
        face.rotateFace();
        face.modifyCube(this.cube);
    }

    /**
     * @param moves
     *              Applies the sequence of moves on the Rubik's Cube
     */
    public void applyMoves(String moves) {
        for (int i = 0; i < moves.length(); i++) {
            switch (moves.charAt(i)) {
                case 'F':
                    applyMoveF();
                    break;
                case 'B':
                    applyMoveB();
                    break;
                case 'L':
                    applyMoveL();
                    break;
                case 'R':
                    applyMoveR();
                    break;
                case 'U':
                    applyMoveU();
                    break;
                case 'D':
                    applyMoveD();
                    break;
            }
        }
    }

    /**
     * returns true if the current state of the Cube is solved,
     * i.e., it is in this state:
     * OOO
     * OOO
     * OOO
     * GGGWWWBBBYYY
     * GGGWWWBBBYYY
     * GGGWWWBBBYYY
     * RRR
     * RRR
     * RRR
     */
    public boolean isSolved() {
        if (this.toString().equals(stateInit)) {
            return true;
        }
        return false;

    }

    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < this.cube.length; i++) {
            for (int j = 0; j < this.cube[i].length; j++) {
                if (this.cube[i][j] == null) {
                    continue;
                }
                result += this.cube[i][j];
            }
            result += "\n";
        }
        return result;
    }

    public RubiksCube deepClone() {
        RubiksCube clonedCube = new RubiksCube();
        for (int i = 0; i < this.cube.length; i++) {
            for (int j = 0; j < this.cube[i].length; j++) {
                clonedCube.cube[i][j] = this.cube[i][j];
            }
        }
        return clonedCube;
    }

    /**
     *
     * @param moves
     * @return the order of the sequence of moves
     */
    public static int order(String moves) {
        RubiksCube tempCube = new RubiksCube();
        Integer order = 0;
        do {
            tempCube.applyMoves(moves);
            order++;
        } while (!tempCube.isSolved());
        return order;
    }
}
