package com.tonyjs.tetrisfx;

import javafx.animation.AnimationTimer;
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
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by tonysaavedra on 6/12/16.
 */
public class TetrisFX extends Application {
    public static final int TILE_SIZE = 40;
    public static final int WIDTH = 10;
    public static final int HEIGHT = 20;
    public static boolean GAME_OVER = false;
    public static boolean ROTATING = false;
    public static boolean DROPPING = false;
    public static boolean SHIFTING = false;
    private double time;

    private AnimationTimer timer;

    private int tetrominoY = 0;
    private int oldTetrominoY = 0;
    private int tetrominoX = 0;
    private int oldTetrominoX = 0;

    public Tetromino originalSet;
    public ArrayList<Tetromino.TetrominoDefinition> tetrominoSet;
    public ArrayList<Tetromino.TetrominoDefinition> playedSet;
    private Tetromino.TetrominoDefinition selected;

    private int[][] selectedMatrix;

    public Tile[][] board = new Tile[HEIGHT][WIDTH];

    private Group tileGroup = new Group();

    Lighting lighting = new Lighting(new Light.Distant(225, 55, Color.WHITE));

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
//                render();
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
                    oldTetrominoX = j;
                }
            }
            oldTetrominoY = i;
        }

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                time += 0.050;
                if (time >= 0.5) {
                    if (!ROTATING && !SHIFTING || !DROPPING) {
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
        } else if (tetrominoY >= (HEIGHT - selectedMatrix.length)) {
            timer.stop();
            DROPPING = false;
            respawn();
        } else if (moveIsLegal) {
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
                    oldTetrominoX = j;
                }
            }
            oldTetrominoY += i;
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
                    if (!board[tetrominoY + matrixSize][j].isAvailable()) {
                        legalMove = false;
                    }
                }
            }
        }
        return legalMove;
    }

    private void respawn() {
        tetrominoY = 0;
        oldTetrominoY = 0;
        tetrominoX = 0;
        oldTetrominoX = 0;
        time = 0;
        timer = null;
        spawnTetrominos();
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
