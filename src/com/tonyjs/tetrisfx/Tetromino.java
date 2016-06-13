package com.tonyjs.tetrisfx;

import javafx.scene.paint.Color;

import java.util.ArrayList;

/**
 * Created by tonysaavedra on 6/12/16.
 */
public class Tetromino {
    private static ArrayList<TetrominoDefinition> pieceSet;

    private static final TetrominoDefinition I = new TetrominoDefinition(new int[][]{
            {1, 1, 1, 1},
            {0, 0, 0, 0}
    }, Color.CYAN);

    private static final TetrominoDefinition J = new TetrominoDefinition(new int[][]{
            {1, 0, 0},
            {1, 1, 1}
    }, Color.BLUE);

    private static final TetrominoDefinition L = new TetrominoDefinition(new int[][]{
            {0, 0, 1},
            {1, 1, 1}
    }, Color.ORANGE);

    private static final TetrominoDefinition O = new TetrominoDefinition(new int[][]{
            {1, 1},
            {1, 1}
    }, Color.YELLOW);

    private static final TetrominoDefinition S = new TetrominoDefinition(new int[][]{
            {0, 1, 1},
            {1, 1, 0}
    }, Color.GREEN);

    private static final TetrominoDefinition T = new TetrominoDefinition(new int[][]{
            {0, 1, 0},
            {1, 1, 1}
    }, Color.PURPLE);

    private static final TetrominoDefinition Z = new TetrominoDefinition(new int[][]{
            {1, 1, 0},
            {0, 1, 1}
    }, Color.ORANGERED);

    public Tetromino() {
        pieceSet = new ArrayList<>();
        pieceSet.add(I);
        pieceSet.add(J);
        pieceSet.add(L);
        pieceSet.add(O);
        pieceSet.add(S);
        pieceSet.add(T);
        pieceSet.add(Z);
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

        private TetrominoDefinition(int[][] matrix, Color color) {
            this.matrix = matrix;
            this.color = color;
        }

        public int[][] getMatrix() {
            return matrix;
        }

        public Color getColor() {
            return color;
        }
    }
}
