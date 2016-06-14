package com.tonyjs.tetrisfx;

import javafx.scene.paint.Color;

import java.util.ArrayList;

/**
 * Created by tonysaavedra on 6/12/16.
 */
public class Tetromino {
    private static ArrayList<TetrominoDefinition> pieceSet;
    private static TetrominoDefinition I;
    private static TetrominoDefinition rotatedI;
    private static TetrominoDefinition J;
    private static TetrominoDefinition rotatedJ;
    private static TetrominoDefinition rotatedJ2;
    private static TetrominoDefinition rotatedJ3;
    private static TetrominoDefinition L;
    private static TetrominoDefinition rotatedL;
    private static TetrominoDefinition rotatedL2;
    private static TetrominoDefinition rotatedL3;
    private static TetrominoDefinition O;
    private static TetrominoDefinition S;
    private static TetrominoDefinition rotatedS;
    private static TetrominoDefinition T;
    private static TetrominoDefinition rotatedT;
    private static TetrominoDefinition rotatedT2;
    private static TetrominoDefinition rotatedT3;
    private static TetrominoDefinition Z;
    private static TetrominoDefinition rotatedZ;

    public void setIBlocks() {
        I = new TetrominoDefinition(new int[][]{
                {0, 0, 0, 0},
                {1, 1, 1, 1}
        }, Color.CYAN);

        rotatedI = new TetrominoDefinition(new int[][]{
                {1, 0},
                {1, 0},
                {1, 0},
                {1, 0}
        }, Color.CYAN);
    }

    public void setJBlocks() {
        J = new TetrominoDefinition(new int[][]{
                {1, 0, 0},
                {1, 1, 1}
        }, Color.BLUE);

        rotatedJ = new TetrominoDefinition(new int[][]{
                {1, 1},
                {1, 0},
                {1, 0}
        }, Color.BLUE);

        rotatedJ2 = new TetrominoDefinition(new int[][]{
                {1, 1, 1},
                {0, 0, 1}
        }, Color.BLUE);

        rotatedJ3 = new TetrominoDefinition(new int[][]{
                {0, 1},
                {0, 1},
                {1, 1}
        }, Color.BLUE);
    }

    public void setLBlocks() {
        L = new TetrominoDefinition(new int[][]{
                {0, 0, 1},
                {1, 1, 1}
        }, Color.rgb(255, 94, 0));

        rotatedL = new TetrominoDefinition(new int[][]{
                {1, 0},
                {1, 0},
                {1, 1}
        }, Color.rgb(255, 94, 0));

        rotatedL2 = new TetrominoDefinition(new int[][]{
                {1, 1, 1},
                {1, 0, 0}
        }, Color.rgb(255, 94, 0));

        rotatedL3 = new TetrominoDefinition(new int[][]{
                {1, 1},
                {0, 1},
                {0, 1}
        }, Color.rgb(255, 94, 0));
    }

    public void setOBlock() {
        O = new TetrominoDefinition(new int[][]{
                {1, 1},
                {1, 1}
        }, Color.YELLOW);
    }

    public void setSBlocks() {
        S = new TetrominoDefinition(new int[][]{
                {0, 1, 1},
                {1, 1, 0}
        }, Color.GREEN);

        rotatedS = new TetrominoDefinition(new int[][]{
                {1, 0},
                {1, 1},
                {0, 1}
        }, Color.GREEN);
    }

    public void setTBlocks() {
        T = new TetrominoDefinition(new int[][]{
                {0, 1, 0},
                {1, 1, 1}
        }, Color.PURPLE);

        rotatedT = new TetrominoDefinition(new int[][]{
                {1, 0},
                {1, 1},
                {1, 0}
        }, Color.PURPLE);

        rotatedT2 = new TetrominoDefinition(new int[][]{
                {1, 1, 1},
                {0, 1, 0}
        }, Color.PURPLE);

        rotatedT3 = new TetrominoDefinition(new int[][]{
                {0, 1},
                {1, 1},
                {0, 1}
        }, Color.PURPLE);
    }

    public void setZBlocks() {
        Z = new TetrominoDefinition(new int[][]{
                {1, 1, 0},
                {0, 1, 1}
        }, Color.RED);

        rotatedZ = new TetrominoDefinition(new int[][]{
                {0, 1},
                {1, 1},
                {1, 0}
        }, Color.RED);
    }

    public Tetromino() {
        setIBlocks();
        setJBlocks();
        setLBlocks();
        setOBlock();
        setSBlocks();
        setTBlocks();
        setZBlocks();

        pieceSet = new ArrayList<>();
        pieceSet.add(I);
        pieceSet.get(0).setNext(rotatedI);

        pieceSet.add(J);
        pieceSet.get(1).setNext(rotatedJ);
        pieceSet.get(1).setNext(rotatedJ2);
        pieceSet.get(1).setNext(rotatedJ3);

        pieceSet.add(L);
        pieceSet.get(2).setNext(rotatedL);
        pieceSet.get(2).setNext(rotatedL2);
        pieceSet.get(2).setNext(rotatedL3);

        pieceSet.add(O);

        pieceSet.add(S);
        pieceSet.get(4).setNext(rotatedS);

        pieceSet.add(T);
        pieceSet.get(5).setNext(rotatedT);
        pieceSet.get(5).setNext(rotatedT2);
        pieceSet.get(5).setNext(rotatedT3);

        pieceSet.add(Z);
        pieceSet.get(6).setNext(rotatedZ);
    }

    public TetrominoDefinition copy(TetrominoDefinition definition) {
        return new TetrominoDefinition(definition.matrix, definition.color);
    }

    public static ArrayList<TetrominoDefinition> getSet() {
        return pieceSet;
    }

    public static class TetrominoDefinition {
        private final Color color;
        private final int[][] matrix;
        private ArrayList<TetrominoDefinition> next;
        private int currentIndex = 0;

        private TetrominoDefinition(int[][] matrix, Color color) {
            this.matrix = matrix;
            this.color = color;
            this.next = new ArrayList<TetrominoDefinition>();
        }

        public void setNext(TetrominoDefinition nextItem) {
            this.next.add(nextItem);
        }

        public int[][] getMatrix() {
            return matrix;
        }

        public Color getColor() {
            return color;
        }

        public int[][] rotate() {
            if (currentIndex != next.size()) {
                int[][] toReturn = next.get(currentIndex).matrix;
                currentIndex++;
                return toReturn;
            } else {
                currentIndex = 0;
                return matrix;
            }
        }
    }
}
