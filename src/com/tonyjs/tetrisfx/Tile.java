package com.tonyjs.tetrisfx;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Created by tonysaavedra on 6/12/16.
 */
public class Tile extends Rectangle{
    public Tile(int x, int y) {
        setWidth(TetrisFX.TILE_SIZE);
        setHeight(TetrisFX.TILE_SIZE);
        relocate(x * TetrisFX.TILE_SIZE, y * TetrisFX.TILE_SIZE);
        setFill(Color.BLACK);
        setStroke(Color.GRAY);
    }
}
