package com.tonyjs.tetrisfx;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
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
    private double time;

    private AnimationTimer timer;

    private int tetrominoY = 0;
    private int oldTetrominoY = 0;

    public Tetromino originalSet;
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
        playedSet = originalSet.getSet();

        spawnTetrominos();
    }

    private void spawnTetrominos() {
        Random r = new Random();
        selected = playedSet.get(r.nextInt(playedSet.size()));

        selectedMatrix = selected.getMatrix();
        for (int i = 0; i < selectedMatrix.length; i++) {
            for (int j = 0; j < selectedMatrix[0].length; j++) {
                if (selectedMatrix[i][j] != 0) {
                    board[i][j].setFill(selected.getColor());
                    board[i][j].setEffect(lighting);
                }
            }
        }

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                time += 0.010;
                if (time >= 0.5) {
                    moveDown();
                    render();
                    time = 0;
                }
            }
        };
        timer.start();
    }

    private void moveDown() {
        if (tetrominoY >= HEIGHT - selectedMatrix.length) {
            timer.stop();
            respawn();
        }
        for (int i = 0; i < selectedMatrix.length; i++) {
            for (int j = 0; j < selectedMatrix[0].length; j++) {
                if (selectedMatrix[i][j] != 0) {
                    board[oldTetrominoY + i][j].setFill(Color.BLACK);
                    board[oldTetrominoY + i][j].setEffect(null);
                }
            }
            tetrominoY += i;
        }
    }

    private void render() {
        for (int i = 0; i < selectedMatrix.length; i++) {
            for (int j = 0; j < selectedMatrix[0].length; j++) {
                if (selectedMatrix[i][j] != 0) {
                    board[tetrominoY + i][j].setFill(selected.getColor());
                    board[tetrominoY + i][j].setEffect(lighting);
                }
            }
            oldTetrominoY += i;
        }
    }

    private void respawn() {
        tetrominoY = 0;
        oldTetrominoY = 0;
        time = 0;
        timer = null;
        spawnTetrominos();
    }

    private Parent getContent() {
        Pane root = new Pane();
        root.getChildren().add(tileGroup);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile(x, y);
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
