package com.tonyjs.tetrisfx;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by tonysaavedra on 6/12/16.
 */
public class TetrisFX extends Application {
    public static final int TILE_SIZE = 32;
    public static final int WIDTH = 11;
    public static final int HEIGHT = 24;
    public static boolean GAME_OVER = false;
    public static boolean ROTATED = false;
    public static boolean DROPPING = false;
    public static boolean SHIFTING = false;
    private double time, shiftTime;
    private Timeline timeline;

    private AnimationTimer timer;

    private int tetrominoY = 0;
    private int tetrominoX = 0;
    private int shiftDifference = 0;

    public Tetromino originalSet;
    public ArrayList<Tetromino.TetrominoDefinition> tetrominoSet;
    public ArrayList<Tetromino.TetrominoDefinition> playedSet;
    private Tetromino.TetrominoDefinition selected;

    private static int[][] selectedMatrix;

    public Tile[][] board = new Tile[HEIGHT][WIDTH];

    private Group tileGroup = new Group();

    private Lighting lighting = new Lighting(new Light.Distant(225, 55, Color.WHITE));

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = getContent();
        primaryStage.setTitle("TetrisFX");
        primaryStage.setScene(new Scene(root, 10 * TILE_SIZE, HEIGHT * TILE_SIZE));
        primaryStage.setResizable(false);
        primaryStage.show();

        originalSet = new Tetromino();
        tetrominoSet = originalSet.getSet();
        playedSet = new ArrayList<>();

        spawnTetrominos();

        primaryStage.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.UP) {
                ROTATED = true;
                rotateBrick();
                render();
            } else if (e.getCode() == KeyCode.LEFT) {
                SHIFTING = true;
                if (tetrominoX > 0 && leftShiftIsLegal()) {
                    moveLeft();
                }
            } else if (e.getCode() == KeyCode.RIGHT) {
                SHIFTING = true;
                if (tetrominoX + selectedMatrix[0].length < WIDTH &&
                        rightShiftIsLegal()) {
                    moveRight();
                }
            } else if (e.getCode() == KeyCode.DOWN) {
                moveDown();
                render();
            } else if (e.getCode() == KeyCode.SPACE) {
                dropBrick();
            }
            SHIFTING = false;
        });

    }

    private void rotateBrick() {
        clearMatrix();
        int[][] rotatedMatrix = selected.rotate();
        selectedMatrix = rotatedMatrix;
        if (tetrominoX >= 8) {
            if (selected.getColor() == Color.CYAN ) {
                tetrominoX-=3;
            } else if (selected.getColor() != Color.YELLOW) {
                tetrominoX-=1;
            }
        }
        for (int i = 0; i < rotatedMatrix.length; i++) {
            for (int j = 0; j < rotatedMatrix[0].length; j++) {
                board[tetrominoY + i][tetrominoX + j].setEffect(rotatedMatrix[i][j] == 0 ? null : lighting);
                board[tetrominoY + i][tetrominoX + j].setFill(rotatedMatrix[i][j] == 0 ? Color.BLACK : selected.getColor());
                board[tetrominoY + i][tetrominoX + j].setAvailable(rotatedMatrix[i][j] == 0);
            }
        }
    }

    private void clearMatrix() {
        for (int i = 0; i < selectedMatrix.length ; i++) {
            for (int j = 0; j < selectedMatrix[0].length; j++) {
                if (selectedMatrix[i][j] != 0) {
                    board[tetrominoY + i][tetrominoX + j].setFill(Color.BLACK);
                    board[tetrominoY + i][tetrominoX + j].setEffect(null);
                    board[tetrominoY + i][tetrominoX + j].setAvailable(true);
                }
            }
        }
    }

    private void moveLeft() {
        clearMatrix();
        --tetrominoX;
        shiftBrick();
    }

    private void moveRight() {
        clearMatrix();
        if (tetrominoX < WIDTH - selectedMatrix[0].length && selected.getColor() == Color.CYAN
                && selectedMatrix[0].length == 2) {
            ++tetrominoX;
        } else if (tetrominoX < WIDTH - selectedMatrix[0].length - 1) {
            ++tetrominoX;
        }
        shiftBrick();
    }

    public void shiftBrick() {
        for (int i = 0; i < selectedMatrix.length ; i++) {
            for (int j = 0; j < selectedMatrix[0].length; j++) {
                if (selectedMatrix[i][j] != 0) {
                    board[tetrominoY + i][tetrominoX + j].setFill(selected.getColor());
                    board[tetrominoY + i][tetrominoX + j].setEffect(lighting);
                    board[tetrominoY + i][tetrominoX + j].setAvailable(false);
                }
            }
        }
    }

    private void dropBrick() {
        DROPPING = true;
        while (DROPPING) {
            moveDown();
            render();
        }
    }

    private void spawnTetrominos() {
        Random r = new Random();
        selected = tetrominoSet.get(r.nextInt(tetrominoSet.size()));
        playedSet.add(selected);
        selectedMatrix = selected.getMatrix();

        for (int i = 0; i < selectedMatrix.length; i++) {
            for (int j = 0; j < selectedMatrix[0].length; j++) {
                if (selectedMatrix[i][j] != 0) {
                        board[i][j].setFill(selected.getColor());
                        board[i][j].setEffect(lighting);
                        board[i][j].setAvailable(false);
                }
            }
        }

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                time += 0.015;
                if (time >= 0.5) {
                    if (!SHIFTING && !DROPPING) {
                        moveDown();
                        render();
                    }
                    time = 0;
                }
            }
        };
        timer.start();
    }

    private void moveDown() {
        boolean moveIsLegal = moveIsLegal();

        if ((tetrominoY <= 1 && !moveIsLegal)) {
            GAME_OVER = true;
            timer.stop();
            Platform.exit();
        } else if (tetrominoY >= (HEIGHT - selectedMatrix.length) || !moveIsLegal) {
            timer.stop();
            DROPPING = false; ROTATED = false;
            checkRows(); respawn();
        } else {
            for (int i = 0; i < selectedMatrix.length ; i++) {
                for (int j = 0; j < selectedMatrix[0].length; j++) {
                    if (selectedMatrix[i][j] != 0) {
                        board[tetrominoY + i][tetrominoX + j].setFill(Color.BLACK);
                        board[tetrominoY + i][tetrominoX + j].setEffect(null);
                        board[tetrominoY + i][tetrominoX + j].setAvailable(true);
                    }
                }
            }
            tetrominoY++;
        }
    }

    private void render() {
        for (int i = 0; i < selectedMatrix.length; i++) {
            for (int j = 0; j < selectedMatrix[0].length; j++) {
                if (selectedMatrix[i][j] != 0) {
                    board[tetrominoY + i][tetrominoX + j].setFill(selected.getColor());
                    board[tetrominoY + i][tetrominoX + j].setEffect(lighting);
                    board[tetrominoY + i][tetrominoX + j].setAvailable(false);
                }
            }
        }
    }

    private boolean moveIsLegal() {
        if (playedSet.size() == 1) {
            return true;
        }

        boolean legalMove = true;

        int matrixSize = selectedMatrix.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < selectedMatrix[0].length; j++) {
                if (tetrominoY < HEIGHT - matrixSize) {
                    if (i < matrixSize - 1) {
                        if (selectedMatrix[i][j] != 0 && selectedMatrix[i + 1][j] == 0) {
                            if (!board[tetrominoY + i + 1][tetrominoX + j].isAvailable()) {
                                legalMove = false;
                            }
                        }
                    } else {
                        if (selectedMatrix[i][j] != 0) {
                            if (!board[tetrominoY + i + 1][tetrominoX + j].isAvailable()) {
                                legalMove = false;
                            }
                        }
                    }
                }
            }
        }
        return legalMove;
    }

    private boolean rightShiftIsLegal() {
        boolean legalMove = true;
        for (int i = tetrominoY; i < tetrominoY + selectedMatrix.length; i++) {
            for (int j = tetrominoX + selectedMatrix[0].length - 1; j < tetrominoX + selectedMatrix[0].length; j++) {
                if (!board[i][j + 1].isAvailable()) {
                    legalMove = false;
                }
            }
        }
        return legalMove;
    }

    private boolean leftShiftIsLegal() {
        boolean legalMove = true;
        for (int i = tetrominoY; i < tetrominoY + selectedMatrix.length; i++) {
            for (int j = tetrominoX; j < tetrominoX + 1; j++) {
                if (!board[i][j - 1].isAvailable()) {
                    legalMove = false;
                }
            }
        }
        return legalMove;
    }

    private void respawn() {
        tetrominoY = 0;
        tetrominoX = 0;
        time = 0;
        timer = null;
        spawnTetrominos();
    }

    private void checkRows() {
        boolean fullRow = true;
        ParallelTransition deleteRowTransition = new ParallelTransition();
        int topRow = 0;

        for (int i = HEIGHT - 1; i >= tetrominoY; i--) {
            fullRow = true;
            for (int j = 0; j < WIDTH-1; j++) {
                if (board[i][j].isAvailable()) {
                    fullRow = false;
                }
            }
            if (fullRow) {
                topRow = i;
                shiftDifference++;
                deleteRowTransition.getChildren().add(deleteRow(i));
            }
        }

        if (deleteRowTransition.getChildren().size() > 0) {
            deleteRowTransition.play();
            final int row = topRow;
            deleteRowTransition.setOnFinished(e -> {
                deleteRowTransition.getChildren().clear();
                shiftRowsAfterDeletion(row, shiftDifference);
                shiftDifference = 0;
            });
        }
    }

    private Animation deleteRow(int rowIndex) {
        ParallelTransition parallelTransition = new ParallelTransition();
        for (int i = rowIndex; i >= rowIndex; i--) {
            for (int j = 0; j < WIDTH-1; j++) {
                if (i > 0) {
                    if (i == rowIndex && board[i][j] != null) {
                        final int colIndex = j;
                        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.30), board[i][j]);
                        fadeTransition.setToValue(0);
                        fadeTransition.setCycleCount(2);
                        fadeTransition.setAutoReverse(true);
                        fadeTransition.setOnFinished(e -> {
                            board[rowIndex][colIndex].setFill(Color.BLACK);
                            board[rowIndex][colIndex].setEffect(null);
                            board[rowIndex][colIndex].setAvailable(true);
                            parallelTransition.getChildren().clear();
                        });
                        parallelTransition.getChildren().add(fadeTransition);
                    }
                }
            }
        }
        return parallelTransition;
    }

    private void shiftRowsAfterDeletion(int rowIndex, int shiftDifferenceY) {
        for (int k = rowIndex - 1; k > 3; k--) {
            for (int colIndex = 0; colIndex < WIDTH - 1; colIndex++) {
                    Paint color = board[k][colIndex].getFill();
                    boolean indexIsAvailable = board[k][colIndex].isAvailable();
                    board[k + shiftDifferenceY][colIndex].setFill(indexIsAvailable ? Color.BLACK : color);
                    board[k + shiftDifferenceY][colIndex].setEffect(indexIsAvailable ? null : lighting);
                    board[k + shiftDifferenceY][colIndex].setAvailable(indexIsAvailable);
            }
        }
    }

    private Parent getContent() {
        Pane root = new Pane();
        root.getChildren().add(tileGroup);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile(x, y, true);
                board[y][x] = tile;
                tileGroup.getChildren().add(tile);
            }
        }
        return root;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
