package com.makeitwash.world;

import com.makeitwash.entities.PlaceableEntity;
import com.makeitwash.entities.ConveyorBelt;
import com.makeitwash.entities.ConveyorItem;
import com.makeitwash.entities.ConveyorItem.Direction;

import java.util.ArrayList;
import java.util.List;

public class Grid {

    public static final int CELL_SIZE = 64;
    public static final int WIDTH     = 20;
    public static final int HEIGHT    = 12;

    private final PlaceableEntity[][] cells;

    // FIX #8: List tipizzata, niente raw type
    private final List<ConveyorItem> items = new ArrayList<>();

    public Grid() {
        this.cells = new PlaceableEntity[WIDTH][HEIGHT];
    }

    public boolean hasConveyorAt(int x, int y) {
        if (!isValid(x, y)) return false;
        return cells[x][y] instanceof ConveyorBelt;
    }

    public ConveyorBelt getConveyorAt(int x, int y) {
        if (!isValid(x, y)) return null;
        if (cells[x][y] instanceof ConveyorBelt cb) return cb;
        return null;
    }

    public boolean isEmpty(int x, int y) {
        return isValid(x, y) && cells[x][y] == null;
    }

    public boolean place(PlaceableEntity entity, int x, int y) {
        if (!isValid(x, y) || !isEmpty(x, y)) return false;
        cells[x][y] = entity;
        entity.setGridPosition(x, y);
        if (entity instanceof ConveyorBelt cb) {
            cb.updateConnections(this);
            notifyAdjacentConveyors(x, y);
        }
        return true;
    }

    private void notifyAdjacentConveyors(int x, int y) {
        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        for (int[] d : dirs) {
            ConveyorBelt adj = getConveyorAt(x + d[0], y + d[1]);
            if (adj != null) adj.updateConnections(this);
        }
    }

    public PlaceableEntity remove(int x, int y) {
        if (!isValid(x, y)) return null;
        PlaceableEntity e = cells[x][y];
        cells[x][y] = null;
        if (e instanceof ConveyorBelt) notifyAdjacentConveyors(x, y);
        return e;
    }

    public void updateAllConveyorConnections() {
        for (int x = 0; x < WIDTH; x++)
            for (int y = 0; y < HEIGHT; y++) {
                ConveyorBelt cb = getConveyorAt(x, y);
                if (cb != null) cb.updateConnections(this);
            }
    }

    public void addItem(ConveyorItem item) { items.add(item); }

    public void updateItems(float delta) {
        for (int i = items.size() - 1; i >= 0; i--) {
            ConveyorItem item = items.get(i);
            item.update(delta);

            if (!item.isMoving()) {
                float itemX = item.getX();
                float itemY = item.getY();

                // FIX #6: conversione griglia più robusta con floor + 0.5f offset
                int gx = (int) Math.floor(itemX / CELL_SIZE);
                int gy = (int) Math.floor(itemY / CELL_SIZE);

                ConveyorBelt conveyor = getConveyorAt(gx, gy);
                if (conveyor != null) {
                    moveItemToNextConveyor(item, gx, gy, conveyor);
                } else {
                    // FIX #2: cleanup corretto prima della rimozione
                    item.onDestroy();
                    items.remove(i);
                }
            }
        }
    }

    private void moveItemToNextConveyor(ConveyorItem item, int gx, int gy, ConveyorBelt conveyor) {
        int       nextX   = gx;
        int       nextY   = gy;
        Direction newDir  = item.getDirection();

        if (conveyor.isConnectedEast()) {
            nextX  = gx + 1;
            newDir = Direction.EAST;
        } else if (conveyor.isConnectedWest()) {
            nextX  = gx - 1;
            newDir = Direction.WEST;
        } else if (conveyor.isConnectedNorth()) {
            nextY  = gy + 1;
            newDir = Direction.NORTH;
        } else if (conveyor.isConnectedSouth()) {
            nextY  = gy - 1;
            newDir = Direction.SOUTH;
        } else {
            return; // nessuna connessione attiva, item resta fermo
        }

        // FIX #3: bounds check PRIMA di calcolare il target
        if (!isValid(nextX, nextY)) {
            item.onDestroy();
            items.remove(item);
            return;
        }

        float targetX = nextX * CELL_SIZE + CELL_SIZE / 2f;
        float targetY = nextY * CELL_SIZE + CELL_SIZE / 2f;

        // FIX #7: aggiorna anche la direction dell'item
        item.setTarget(targetX, targetY, newDir);
    }

    public List<ConveyorItem> getItems() { return items; }

    public void clearItems() {
        for (ConveyorItem item : items) item.onDestroy();
        items.clear();
    }

    public PlaceableEntity get(int x, int y) {
        if (!isValid(x, y)) return null;
        return cells[x][y];
    }

    public int   toGridX(float worldX) { return (int)(worldX / CELL_SIZE); }
    public int   toGridY(float worldY) { return (int)(worldY / CELL_SIZE); }
    public float toPixelX(int gridX)   { return gridX * CELL_SIZE; }
    public float toPixelY(int gridY)   { return gridY * CELL_SIZE; }

    public boolean isValid(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    public void update(float delta) {
        for (int x = 0; x < WIDTH; x++)
            for (int y = 0; y < HEIGHT; y++)
                if (cells[x][y] != null) cells[x][y].update(delta);
    }
}