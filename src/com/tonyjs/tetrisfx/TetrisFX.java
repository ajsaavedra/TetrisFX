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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by tonysaavedra on 6/12/16.
 */
public class TetrisFX extends Application {
    public static final int TILE_SIZE = 32;
    public static final int WIDTH = 11;
    public static final int HEIGHT = 24;
    public static final int APPWIDTH = 14;
    public static final int SIDE_TILE_HEIGHT = 4;
    public static final int SIDE_TILE_WIDTH = 4;
    public static boolean GAME_OVER = false;
    public static boolean ROTATED = false;
    public static boolean DROPPING = false;
    public static boolean SHIFTING = false;
    public static boolean HOLDING = false;
    private double time;
    private AnimationTimer timer;

    private int tetrominoY = 0;
    private int tetrominoX = 0;
    private int shiftDifference = 0;
    private int playerPoints = 0;
    private int playerLevelNum = 1;
    private int timesRowWasDeleted = 0;

    public Tetromino originalSet;
    public ArrayList<Tetromino.TetrominoDefinition> tetrominoSet;
    public ArrayList<Tetromino.TetrominoDefinition> playedSet;
    private Tetromino.TetrominoDefinition selected, queuedBrick, heldBrick;

    private static int[][] selectedMatrix, queuedMatrix, heldMatrix;

    public Tile[][] board = new Tile[HEIGHT][WIDTH];
    public Tile[][] nextBrickBoard = new Tile[SIDE_TILE_HEIGHT][SIDE_TILE_WIDTH];
    public Tile[][] heldBrickBoard = new Tile[SIDE_TILE_HEIGHT][SIDE_TILE_WIDTH];

    private Group tileGroup = new Group();
    private Group sideBarTiles = new Group();
    private Group heldBrickTiles = new Group();

    private Lighting lighting = new Lighting(new Light.Distant(225, 55, Color.WHITE));
    private Text playerLevel, pointsLabel;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = getContent();

        primaryStage.setTitle("TetrisFX");
        Scene main = new Scene(root, APPWIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        primaryStage.setScene(main);
        primaryStage.setResizable(false);
        primaryStage.show();

        URL url = this.getClass().getResource("/css/styles.css");
        String css = url.toExternalForm();
        main.getStylesheets().add(css);


        originalSet = new Tetromino();
        tetrominoSet = originalSet.getSet();
        playedSet = new ArrayList<>();

        setInitialBricks();
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
            } else if (e.getCode() == KeyCode.H) {
                holdCurrentBrick();
                render();
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

    private void clearSideBarTiles() {
        for (int i = 0; i < SIDE_TILE_HEIGHT ; i++) {
            for (int j = 0; j < SIDE_TILE_WIDTH; j++) {
                nextBrickBoard[i][j].setFill(Color.rgb(30, 30, 30));
                nextBrickBoard[i][j].setEffect(null);
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
        swapQueuedBrick();
        selectedMatrix = selected.getMatrix();
        clearSideBarTiles();

        for (int i = 0; i < selectedMatrix.length; i++) {
            for (int j = 0; j < selectedMatrix[0].length; j++) {
                if (selectedMatrix[i][j] != 0) {
                        board[i][j].setFill(selected.getColor());
                        board[i][j].setEffect(lighting);
                        board[i][j].setAvailable(false);
                }
            }
        }

        paintNextBrickTiles();

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (playerLevelNum < 2) {
                    time += 0.015;
                } else if (playerLevelNum < 3) {
                    time += 0.018;
                } else if (playerLevelNum < 4) {
                    time += 0.020;
                } else if (playerLevelNum < 5) {
                    time += 0.025;
                } else if (playerLevelNum < 6) {
                    time += 0.028;
                } else if (playerLevelNum < 7) {
                    time += 0.030;
                } else if (playerLevelNum < 8) {
                    time += 0.032;
                } else if (playerLevelNum < 9) {
                    time += 0.035;
                } else if (playerLevelNum < 10) {
                    time += 0.040;
                } else if (playerLevelNum < 11) {
                    time += 0.045;
                }

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

    private void setInitialBricks() {
        Random r = new Random();
        selected = tetrominoSet.get(r.nextInt(tetrominoSet.size()));
        queueNextBrick();
    }

    private void queueNextBrick() {
        Random r = new Random();
        queuedBrick = tetrominoSet.get(r.nextInt(tetrominoSet.size()));
        queuedMatrix = queuedBrick.getMatrix();
    }

    private void swapQueuedBrick() {
        selected = queuedBrick;
        selectedMatrix = selected.getMatrix();
        queueNextBrick();
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
            updatePlayerLevel();
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

    private void updatePlayerLevel() {
        playerLevel.setText("Level " + Integer.toString(playerLevelNum));
        pointsLabel.setText("SCORE: " + Integer.toString(playerPoints));
    }

    private void updateTotalScore(int playerLevel, int rowsCleared) {
        int y = 0;
        switch (rowsCleared) {
            case 1:
                y = 40;
                break;
            case 2:
                y = 100;
                break;
            case 3:
                y = 300;
                break;
            case 4:
                y = 1200;
                break;
        }
        playerPoints += y * (playerLevel + 1);
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
        updateTotalScore(playerLevelNum, shiftDifferenceY);
        timesRowWasDeleted++;
        if (timesRowWasDeleted == 10) {
            playerLevelNum++;
            updatePlayerLevel();
            timesRowWasDeleted = 0;
        }
    }

    private void paintNextBrickTiles() {
        for (int i = 0; i < queuedMatrix.length; i++) {
            for (int j = 0; j < queuedMatrix[0].length; j++) {
                if (queuedMatrix[i][j] != 0) {
                    if (queuedBrick.getColor() == Color.YELLOW ||
                            queuedBrick.getColor() == Color.RED ||
                            queuedBrick.getColor() == Color.ORANGERED) {
                        nextBrickBoard[i + 1][j + 1].setFill(queuedBrick.getColor());
                        nextBrickBoard[i + 1][j + 1].setEffect(lighting);
                    } else if (queuedBrick.getColor() != Color.CYAN) {
                        nextBrickBoard[i + 1][j].setFill(queuedBrick.getColor());
                        nextBrickBoard[i + 1][j].setEffect(lighting);
                    } else {
                        nextBrickBoard[i][j].setFill(queuedBrick.getColor());
                        nextBrickBoard[i][j].setEffect(lighting);
                    }
                }
            }
        }
    }

    private void holdCurrentBrick() {
        clearHeldBrickBoard();
        clearMatrix();

        for (int i = 0; i < selectedMatrix.length; i++) {
            for (int j = 0; j < selectedMatrix[0].length; j++) {
                if (selectedMatrix[i][j] != 0) {
                    heldBrickBoard[i][j].setFill(selected.getColor());
                    heldBrickBoard[i][j].setEffect(lighting);
                }
            }
        }

        if (!HOLDING) {
            heldBrick = selected;
            swapQueuedBrick();
            clearSideBarTiles();
            paintNextBrickTiles();
            HOLDING = true;
        } else {
            Tetromino.TetrominoDefinition temp = selected;
            selected = heldBrick;
            heldBrick = temp;
            selectedMatrix = selected.getMatrix();
        }
    }

    private void clearHeldBrickBoard() {
        for (int i = 0; i < SIDE_TILE_HEIGHT; i++) {
            for (int j = 0; j < SIDE_TILE_WIDTH; j++) {
                heldBrickBoard[i][j].setFill(Color.rgb(30, 30, 30));
                heldBrickBoard[i][j].setEffect(null);
            }
        }
    }

    private Parent getContent() {
        HBox rootWindow = new HBox();
        Pane root = new Pane();

        root.getChildren().add(tileGroup);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile(x, y, true);
                board[y][x] = tile;
                tileGroup.getChildren().add(tile);
            }
        }

        Parent sideBar = setSideBarContent();
        rootWindow.getChildren().addAll(sideBar, root);
        return rootWindow;
    }

    private Parent setSideBarContent() {
        VBox sideBarStack = new VBox();

        for (int y = 0; y < SIDE_TILE_HEIGHT; y++) {
            for (int x = 0; x < SIDE_TILE_WIDTH; x++) {
                Tile tile = new Tile(x, y);
                nextBrickBoard[y][x] = tile;
                sideBarTiles.getChildren().add(tile);

                Tile heldTile = new Tile(x, y);
                heldBrickBoard[y][x] = heldTile;
                heldBrickTiles.getChildren().add(heldTile);
            }
        }

        Text nextBrick = new Text("Next Brick");
        nextBrick.setFill(Color.WHITE);
        nextBrick.setFont(Font.font("Monaco", FontWeight.THIN, 16));
        nextBrick.setX(15.0f);
        nextBrick.setY(20.0f);

        Pane sideBar = new Pane();

        Rectangle rec = new Rectangle();
        rec.setWidth(5 * TILE_SIZE);
        rec.setHeight(HEIGHT * TILE_SIZE);
        rec.setFill(Color.rgb(30, 30, 30));

        playerLevel = new Text("Level " + Integer.toString(playerLevelNum));
        playerLevel.setFill(Color.WHITE);
        playerLevel.setFont(Font.font("Monaco", FontWeight.EXTRA_BOLD, 20));
        playerLevel.setX(10.0f);
        playerLevel.setY(220.0f);

        pointsLabel = new Text("SCORE: 0");
        pointsLabel.setFill(Color.WHITE);
        pointsLabel.setFont(Font.font("Monaco", FontWeight.THIN, 16));
        pointsLabel.setX(10.0f);
        pointsLabel.setY(240.0f);

        Text heldBrickLabel = new Text("Hold");
        heldBrickLabel.setFill(Color.WHITE);
        heldBrickLabel.setFont(Font.font("Monaco", FontWeight.THIN, 16));
        heldBrickLabel.setX(30.0f);
        heldBrickLabel.setY(500.0f);

        sideBar.getChildren().addAll(rec, nextBrick, playerLevel, pointsLabel, heldBrickLabel);

        sideBarStack.getChildren().addAll(sideBarTiles, sideBar, heldBrickTiles);
        return sideBarStack;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
