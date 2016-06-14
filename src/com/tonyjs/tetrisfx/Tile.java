package com.tonyjs.tetrisfx;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Created by tonysaavedra on 6/12/16.
 */
public class Tile extends Rectangle {
    private boolean available;

    public Tile(int x, int y, boolean available) {
        setWidth(TetrisFX.TILE_SIZE);
        setHeight(TetrisFX.TILE_SIZE);
        relocate(x * TetrisFX.TILE_SIZE, y * TetrisFX.TILE_SIZE);
        setFill(Color.BLACK);
        setStroke(Color.rgb(45, 45, 45));
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
