package com.makeitwash.world;

import com.makeitwash.entities.PlaceableEntity;

public class Grid {
    public static final int CELL_SIZE = 64;
    public static final int WIDTH = 20;
    public static final int HEIGHT = 12;

    private final PlaceableEntity[][] cells;

    public Grid() {
        this.cells = new PlaceableEntity[WIDTH][HEIGHT];
    }

    public boolean isEmpty(int x, int y) {
        return isValid(x, y) && cells[x][y] == null;
    }

    public boolean place(PlaceableEntity entity, int x, int y) {
        if (!isValid(x, y) || !isEmpty(x, y)) return false;
        cells[x][y] = entity;
        entity.setGridPosition(x, y);
        return true;
    }

    public PlaceableEntity remove(int x, int y) {
        if (!isValid(x, y)) return null;
        PlaceableEntity e = cells[x][y];
        cells[x][y] = null;
        return e;
    }

    public PlaceableEntity get(int x, int y) {
        if (!isValid(x, y)) return null;
        return cells[x][y];
    }

    public int toGridX(float worldX) {
        return (int)(worldX / CELL_SIZE);
    }

    public int toGridY(float worldY) {
        return (int)(worldY / CELL_SIZE);
    }

    public float toPixelX(int gridX) {
        return gridX * CELL_SIZE;
    }

    public float toPixelY(int gridY) {
        return gridY * CELL_SIZE;
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    public void update(float delta) {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (cells[x][y] != null) {
                    cells[x][y].update(delta);
                }
            }
        }
    }
}
