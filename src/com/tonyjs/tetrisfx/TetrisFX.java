package com.tonyjs.tetrisfx;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by tonysaavedra on 6/12/16.
 */
public class TetrisFX extends Application {
    public static final int TILE_SIZE = 32;
    public static final int WIDTH = 10;
    public static final int HEIGHT = 24;
    public static boolean GAME_OVER = false;
    public static boolean ROTATING = false;
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

    private int[][] selectedMatrix;

    public Tile[][] board = new Tile[HEIGHT][WIDTH];

    private Group tileGroup = new Group();

    private Lighting lighting = new Lighting(new Light.Distant(225, 55, Color.WHITE));

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = getContent();
        primaryStage.setTitle("TetrisFX");
        primaryStage.setScene(new Scene(root, WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE));
        primaryStage.setResizable(false);
        primaryStage.show();

        originalSet = new Tetromino();
        tetrominoSet = originalSet.getSet();
        playedSet = new ArrayList<>();

        spawnTetrominos();

        primaryStage.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                ROTATING = true;
                clearMatrix();
                selectedMatrix = selected.rotate();
                render();
            } else if (e.getCode() == KeyCode.LEFT) {
                SHIFTING = true;
                moveLeft();
            } else if (e.getCode() == KeyCode.RIGHT) {
                SHIFTING = true;
                moveRight();
            } else if (e.getCode() == KeyCode.DOWN) {
                dropBrick();
            }

            ROTATING = false;
            SHIFTING = false;
        });

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
        if (tetrominoX > 0) {
            clearMatrix();
            --tetrominoX;
            shiftBrick();
        }
    }

    private void moveRight() {
        if (tetrominoX < WIDTH - selectedMatrix[0].length) {
            clearMatrix();
            ++tetrominoX;
            shiftBrick();
        }
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
                    if (!ROTATING && !SHIFTING && !DROPPING) {
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

        if ((tetrominoY <= 0 && !moveIsLegal)) {
            GAME_OVER = true;
            timer.stop();
            Platform.exit();
        } else if (tetrominoY >= (HEIGHT - selectedMatrix.length) || !moveIsLegal) {
            timer.stop();
            DROPPING = false;
            checkRows();
            respawn();
        } else {
            for (int i = 0; i < selectedMatrix.length ; i++) {
                for (int j = 0; j < selectedMatrix[0].length; j++) {
                    if (selectedMatrix[i][j] != 0) {
                        board[tetrominoY + i][tetrominoX + j].setFill(Color.BLACK);
                        board[tetrominoY + i][tetrominoX + j].setEffect(null);
                        board[tetrominoY + i][tetrominoX + j].setAvailable(true);
                    }
                }
                tetrominoY += i;
            }
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
        for (int i = matrixSize - 1; i < matrixSize; i++) {
            for (int j = 0; j < selectedMatrix[0].length; j++) {
                if (selectedMatrix[i][j] != 0) {
                    if ((tetrominoY >= (HEIGHT - matrixSize)) ||
                            !board[tetrominoY + matrixSize][tetrominoX + j].isAvailable()) {
                        legalMove = false;
                    }
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

        for (int i = tetrominoY + selectedMatrix.length-1; i >= tetrominoY; i--) {
            fullRow = true;
            for (int j = 0; j < WIDTH; j++) {
                if (board[i][j].isAvailable()) {
                    fullRow = false;
                }
            }
            if (fullRow) {
                shiftDifference++;
                deleteRowTransition.getChildren().add(deleteRow(i, shiftDifference));
            }
        }

        if (deleteRowTransition.getChildren().size() > 0) {
            deleteRowTransition.play();
            shiftDifference = 0;
        }
    }

    private Animation deleteRow(int rowIndex, int shiftDifferenceX) {
        ParallelTransition parallelTransition = new ParallelTransition();
        for (int i = rowIndex; i >= rowIndex; i--) {
            for (int j = 0; j < WIDTH; j++) {
                if (i > 0) {
                    if (i == rowIndex && board[i][j] != null) {
                        final int colIndex = j;
                        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.2), board[i][j]);
                        fadeTransition.setToValue(0);
                        fadeTransition.setCycleCount(2);
                        fadeTransition.setAutoReverse(true);
                        fadeTransition.setOnFinished(e -> {
                            board[rowIndex][colIndex].setFill(Color.BLACK);
                            board[rowIndex][colIndex].setEffect(null);
                            board[rowIndex][colIndex].setAvailable(true);
                            parallelTransition.getChildren().clear();
                                for (int k = rowIndex-1; k > tetrominoY + selectedMatrix.length - 1; k--) {
                                    if (!board[k][colIndex].isAvailable()) {
                                        Paint color = board[k][colIndex].getFill();
                                        board[k][colIndex].setFill(Color.BLACK);
                                        board[k][colIndex].setEffect(null);
                                        board[k][colIndex].setAvailable(true);
                                        board[k + shiftDifferenceX][colIndex].setFill(color);
                                        board[k + shiftDifferenceX][colIndex].setEffect(lighting);
                                        board[k + shiftDifferenceX][colIndex].setAvailable(false);
                                    }
                                }
                        });
                        parallelTransition.getChildren().add(fadeTransition);
                    }

                }
            }
        }
        return parallelTransition;
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
