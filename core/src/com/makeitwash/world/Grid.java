package com.makeitwash.world;

import com.makeitwash.entities.PlaceableEntity;
import com.makeitwash.entities.ConveyorBelt;
import com.makeitwash.entities.ConveyorBelt.Direction;
import com.makeitwash.entities.ConveyorItem;

import java.util.ArrayList;
import java.util.List;

public class Grid {

    public static final int CELL_SIZE = 64;
    public static final int WIDTH     = 20;
    public static final int HEIGHT    = 12;

    private final PlaceableEntity[][] cells;
    private final List<ConveyorItem>  items = new ArrayList<>();

    public Grid() {
        this.cells = new PlaceableEntity[WIDTH][HEIGHT];
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------
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

    public PlaceableEntity get(int x, int y) {
        if (!isValid(x, y)) return null;
        return cells[x][y];
    }

    // -------------------------------------------------------------------------
    // Piazzamento
    // -------------------------------------------------------------------------

    /**
     * Piazza un'entità senza informazioni sulla direzione di provenienza.
     * Se è un ConveyorBelt usa l'inferimento euristico.
     */
    public boolean place(PlaceableEntity entity, int x, int y) {
        return place(entity, x, y, null);
    }

    /**
     * Piazza un'entità con la direzione esplicita da cui arriva il giocatore.
     * Per i ConveyorBelt questa info viene usata per orientare correttamente
     * il nastro appena posato.
     *
     * @param fromDirection  direzione da cui arriva il cursore/giocatore
     *                       (es. Direction.WEST se il giocatore viene da sinistra)
     */
    public boolean place(PlaceableEntity entity, int x, int y, Direction fromDirection) {
        if (!isValid(x, y) || !isEmpty(x, y)) return false;
        cells[x][y] = entity;
        entity.setGridPosition(x, y);
        if (entity instanceof ConveyorBelt cb) {
            // Aggiorniamo le connessioni con la direzione di piazzamento
            cb.updateConnections(this, fromDirection);
            // Notifichiamo i vicini (che potrebbero cambiare curva/dritto)
            notifyAdjacentConveyors(x, y, fromDirection);
        }
        return true;
    }

    private void notifyAdjacentConveyors(int x, int y, Direction placedFrom) {
        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        for (int[] d : dirs) {
            ConveyorBelt adj = getConveyorAt(x + d[0], y + d[1]);
            if (adj != null) adj.updateConnections(this); // i vicini non hanno fromDirection nota
        }
    }

    // -------------------------------------------------------------------------
    // Rimozione
    // -------------------------------------------------------------------------
    public PlaceableEntity remove(int x, int y) {
        if (!isValid(x, y)) return null;
        PlaceableEntity e = cells[x][y];
        cells[x][y] = null;
        if (e instanceof ConveyorBelt) notifyAdjacentConveyors(x, y, null);
        return e;
    }

    public void updateAllConveyorConnections() {
        for (int x = 0; x < WIDTH; x++)
            for (int y = 0; y < HEIGHT; y++) {
                ConveyorBelt cb = getConveyorAt(x, y);
                if (cb != null) cb.updateConnections(this);
            }
    }

    // -------------------------------------------------------------------------
    // Item management
    // -------------------------------------------------------------------------
    public void addItem(ConveyorItem item) { items.add(item); }

    public List<ConveyorItem> getItems() { return items; }

    public void clearItems() {
        for (ConveyorItem item : items) item.onDestroy();
        items.clear();
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /**
     * Aggiorna tutta la griglia:
     *  1. Avanza il progress globale dei nastri (sincronizzato).
     *  2. Aggiorna le singole entità (macchine, ecc.).
     *  3. Muove gli item sui nastri.
     */
    public void update(float delta) {
        // 1. Tick globale sincronizzato — UNA SOLA chiamata per frame
        ConveyorBelt.tickGlobal(delta);

        // 2. Aggiorna entità
        for (int x = 0; x < WIDTH; x++)
            for (int y = 0; y < HEIGHT; y++)
                if (cells[x][y] != null) cells[x][y].update(delta);

        // 3. Muovi gli item
        updateItems(delta);
    }

    private void updateItems(float delta) {
        for (int i = items.size() - 1; i >= 0; i--) {
            ConveyorItem item = items.get(i);
            item.update(delta);

            if (!item.isMoving()) {
                int gx = (int) Math.floor(item.getX() / CELL_SIZE);
                int gy = (int) Math.floor(item.getY() / CELL_SIZE);

                ConveyorBelt conveyor = getConveyorAt(gx, gy);
                if (conveyor != null) {
                    moveItemToNextConveyor(item, gx, gy, conveyor);
                } else {
                    item.onDestroy();
                    items.remove(i);
                }
            }
        }
    }

    private void moveItemToNextConveyor(ConveyorItem item, int gx, int gy, ConveyorBelt conveyor) {
        // Usiamo la outputDirection del nastro per sapere dove mandare l'item
        Direction out = conveyor.getOutputDirection();

        int nextX = gx;
        int nextY = gy;
        com.makeitwash.entities.ConveyorItem.Direction itemDir;

        switch (out) {
            case EAST:  nextX = gx + 1; itemDir = com.makeitwash.entities.ConveyorItem.Direction.EAST;  break;
            case WEST:  nextX = gx - 1; itemDir = com.makeitwash.entities.ConveyorItem.Direction.WEST;  break;
            case NORTH: nextY = gy + 1; itemDir = com.makeitwash.entities.ConveyorItem.Direction.NORTH; break;
            case SOUTH: nextY = gy - 1; itemDir = com.makeitwash.entities.ConveyorItem.Direction.SOUTH; break;
            default:    return;
        }

        // Bounds check prima di muovere
        if (!isValid(nextX, nextY)) {
            item.onDestroy();
            items.remove(item);
            return;
        }

        float targetX = nextX * CELL_SIZE + CELL_SIZE / 2f;
        float targetY = nextY * CELL_SIZE + CELL_SIZE / 2f;
        item.setTarget(targetX, targetY, itemDir);
    }

    // -------------------------------------------------------------------------
    // Coordinate utils
    // -------------------------------------------------------------------------
    public int   toGridX(float worldX) { return (int)(worldX / CELL_SIZE); }
    public int   toGridY(float worldY) { return (int)(worldY / CELL_SIZE); }
    public float toPixelX(int gridX)   { return gridX * CELL_SIZE; }
    public float toPixelY(int gridY)   { return gridY * CELL_SIZE; }

    public boolean isValid(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }
}