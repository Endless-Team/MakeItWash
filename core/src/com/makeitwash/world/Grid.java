package com.makeitwash.world;

import com.makeitwash.entities.ConveyorBelt;
import com.makeitwash.entities.ConveyorBelt.Direction;
import com.makeitwash.entities.PlaceableEntity;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Griglia di nastri trasportatori.
 *
 * ── SISTEMA FLUSSI ─────────────────────────────────────────────────────────
 * Ogni volta che viene piazzato o rimosso un nastro:
 *  1. Si aggiornano le connessioni fisiche di tutti i vicini.
 *  2. Si esegue propagateFlow() che, tramite BFS, assegna inputDirection e
 *     outputDirection a ogni nastro della catena partendo dai nastri "sorgente"
 *     (quelli con un solo vicino, o senza vicini).
 *
 * Il flusso è direzionale: ogni nastro sa da dove arriva e dove va.
 * Questo permette agli item di seguire la catena con nextGridX/nextGridY().
 *
 * ── PIAZZAMENTO SMART ──────────────────────────────────────────────────────
 * placeConveyor(gridX, gridY, hintDirection) accetta una direzione "suggerita"
 * (es. la direzione in cui si stava costruendo) e la usa come ingresso iniziale
 * per il nuovo nastro se non ci sono vicini con flusso già definito.
 */
public class Grid {

    public static final int WIDTH     = 20;
    public static final int HEIGHT    = 12;
    public static final int CELL_SIZE = 64;

    private final int width;
    private final int height;

    /** Mappa (gridX, gridY) → ConveyorBelt */
    private final Map<Long, ConveyorBelt> belts = new HashMap<>();

    public Grid(int width, int height) {
        this.width  = width;
        this.height = height;
    }

    // =========================================================================
    // Chiave hash per le celle
    // =========================================================================
    private static long key(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    // =========================================================================
    // Accesso
    // =========================================================================
    public boolean hasConveyorAt(int x, int y) {
        return belts.containsKey(key(x, y));
    }

    public ConveyorBelt getConveyorAt(int x, int y) {
        return belts.get(key(x, y));
    }

    public List<ConveyorBelt> getAllBelts() {
        return new ArrayList<>(belts.values());
    }

    // =========================================================================
    // Piazzamento e rimozione
    // =========================================================================

    /**
     * Piazza un nastro in (gridX, gridY).
     *
     * @param gridX         colonna griglia
     * @param gridY         riga griglia
     * @param hintDirection direzione di costruzione suggerita (es. EAST se si
     *                      sta trascinando verso destra). Usata come ingresso
     *                      iniziale se non ci sono vicini con flusso.
     *                      Può essere null (verrà usato WEST come default).
     */
    public ConveyorBelt placeConveyor(int gridX, int gridY, Direction hintDirection) {
        if (belts.containsKey(key(gridX, gridY))) return null;

        ConveyorBelt belt = new ConveyorBelt(gridX, gridY);
        belts.put(key(gridX, gridY), belt);

        // Aggiorna connessioni fisiche del nuovo nastro e dei vicini
        refreshConnections(gridX, gridY);

        // Propaga il flusso sull'intera rete
        propagateFlow(hintDirection != null ? hintDirection : Direction.WEST);

        return belt;
    }

    /** Rimuove il nastro in (gridX, gridY) e ricalcola flusso. */
    public void removeConveyor(int gridX, int gridY) {
        belts.remove(key(gridX, gridY));
        refreshConnections(gridX, gridY);
        propagateFlow(Direction.WEST);
    }

    // =========================================================================
    // Aggiornamento connessioni fisiche
    // =========================================================================
    private void refreshConnections(int gridX, int gridY) {
        // Aggiorna il nastro in (gridX, gridY) se esiste
        ConveyorBelt center = belts.get(key(gridX, gridY));
        if (center != null) center.updateConnections(this);

        // Aggiorna i 4 vicini
        for (Direction d : Direction.values()) {
            ConveyorBelt neighbor = belts.get(key(gridX + d.dx(), gridY + d.dy()));
            if (neighbor != null) neighbor.updateConnections(this);
        }
    }

    // =========================================================================
    // PROPAGAZIONE FLUSSO (BFS)
    // =========================================================================
    /**
     * Calcola inputDirection e outputDirection per tutti i nastri nella rete.
     *
     * Algoritmo:
     *  1. Trova i nastri "sorgente" = nastri con esattamente 1 connessione
     *     (inizio o fine di una catena), oppure nastri isolati.
     *  2. Da ogni sorgente, fa BFS propagando il flusso verso i vicini.
     *  3. I nastri in loop (es. nastri in cerchio) vengono gestiti scegliendo
     *     un punto di partenza arbitrario nel loop.
     *
     * @param defaultInput direzione di ingresso per sorgenti senza contesto
     */
    public void propagateFlow(Direction defaultInput) {
        Set<Long> visited = new HashSet<>();

        // Fase 1: sorgenti (1 connessione o 0)
        Queue<ConveyorBelt> queue = new ArrayDeque<>();
        for (ConveyorBelt belt : belts.values()) {
            int conn = countConnections(belt);
            if (conn <= 1) {
                // Sorgente: ingresso dal lato libero o con defaultInput
                Direction input = inferSourceInput(belt, defaultInput);
                belt.applyFlow(input);
                visited.add(key(belt.getGridX(), belt.getGridY()));
                // Aggiungi il vicino in uscita alla coda
                ConveyorBelt next = belts.get(
                    key(belt.nextGridX(), belt.nextGridY()));
                if (next != null && !visited.contains(key(next.getGridX(), next.getGridY()))) {
                    queue.add(next);
                }
            }
        }

        // Fase 2: BFS dalla coda
        while (!queue.isEmpty()) {
            ConveyorBelt belt = queue.poll();
            long k = key(belt.getGridX(), belt.getGridY());
            if (visited.contains(k)) continue;
            visited.add(k);

            // Trova il vicino già visitato che alimenta questo nastro
            Direction inputFrom = findInputFrom(belt, visited);
            if (inputFrom != null) {
                Direction actualInput = inputFrom;
                belt.applyFlow(actualInput);
            }

            // Propaga al successore
            ConveyorBelt next = belts.get(key(belt.nextGridX(), belt.nextGridY()));
            if (next != null && !visited.contains(key(next.getGridX(), next.getGridY()))) {
                queue.add(next);
            }
        }

        // Fase 3: nastri in loop non ancora visitati (scegli punto arbitrario)
        for (ConveyorBelt belt : belts.values()) {
            long k = key(belt.getGridX(), belt.getGridY());
            if (!visited.contains(k)) {
                belt.applyFlow(defaultInput);
                visited.add(k);
                ConveyorBelt next = belts.get(key(belt.nextGridX(), belt.nextGridY()));
                if (next != null && !visited.contains(key(next.getGridX(), next.getGridY()))) {
                    queue.add(next);
                }
                // Svuota la coda per questo sub-loop
                while (!queue.isEmpty()) {
                    ConveyorBelt b = queue.poll();
                    long bk = key(b.getGridX(), b.getGridY());
                    if (visited.contains(bk)) continue;
                    visited.add(bk);
                    Direction inp = findInputFrom(b, visited);
                    if (inp != null) {
                        Direction actualInput = inp;
                        b.applyFlow(actualInput);
                    }
                    ConveyorBelt nx = belts.get(key(b.nextGridX(), b.nextGridY()));
                    if (nx != null && !visited.contains(key(nx.getGridX(), nx.getGridY())))
                        queue.add(nx);
                }
            }
        }
    }

    /**
     * Per una sorgente (1 o 0 connessioni), inferisce la direzione di ingresso.
     * Se il nastro ha un vicino, l'ingresso viene dal lato opposto al vicino.
     * Altrimenti usa defaultInput.
     */
    private Direction inferSourceInput(ConveyorBelt belt, Direction defaultInput) {
        for (Direction d : Direction.values()) {
            if (isConnectedIn(belt, d)) {
                return d;
            }
        }
        return defaultInput;
    }

    /**
     * Trova la direzione di ingresso di 'belt' cercando nei suoi vicini
     * quale è già stato visitato e il cui outputDirection punta verso belt.
     */
    private Direction findInputFrom(ConveyorBelt belt, Set<Long> visited) {
        for (Direction d : Direction.values()) {
            int nx = belt.getGridX() + d.dx();
            int ny = belt.getGridY() + d.dy();
            if (!visited.contains(key(nx, ny))) continue;
            ConveyorBelt neighbor = belts.get(key(nx, ny));
            if (neighbor == null) continue;
            if (neighbor.nextGridX() == belt.getGridX() &&
                neighbor.nextGridY() == belt.getGridY()) {
                return d;
            }
        }
        return null;
    }

    private boolean isConnectedIn(ConveyorBelt belt, Direction d) {
        switch (d) {
            case NORTH: return belt.isConnectedNorth();
            case SOUTH: return belt.isConnectedSouth();
            case EAST:  return belt.isConnectedEast();
            case WEST:  return belt.isConnectedWest();
            default:    return false;
        }
    }

    private int countConnections(ConveyorBelt belt) {
        int c = 0;
        if (belt.isConnectedNorth()) c++;
        if (belt.isConnectedSouth()) c++;
        if (belt.isConnectedEast())  c++;
        if (belt.isConnectedWest())  c++;
        return c;
    }

    // =========================================================================
    // Update / Render
    // =========================================================================
    public void update(float delta) {
        ConveyorBelt.tickGlobal(delta);
    }

    public void render(SpriteBatch batch) {
        for (ConveyorBelt belt : belts.values()) {
            belt.render(batch);
        }
    }

    // =========================================================================
    // Getters
    // =========================================================================
    public int getWidth()  { return width; }
    public int getHeight() { return height; }

    // =========================================================================
    // Coordinate conversion
    // =========================================================================
    public int toGridX(float pixelX) {
        return (int)(pixelX / CELL_SIZE);
    }

    public int toGridY(float pixelY) {
        return (int)(pixelY / CELL_SIZE);
    }

    public float toPixelX(int gridX) {
        return gridX * CELL_SIZE;
    }

    public float toPixelY(int gridY) {
        return gridY * CELL_SIZE;
    }

    public boolean isValid(int gridX, int gridY) {
        return gridX >= 0 && gridX < width && gridY >= 0 && gridY < height;
    }

    public boolean isEmpty(int gridX, int gridY) {
        return isValid(gridX, gridY) && !hasConveyorAt(gridX, gridY);
    }

    public PlaceableEntity get(int gridX, int gridY) {
        return getConveyorAt(gridX, gridY);
    }

    public boolean place(PlaceableEntity entity, int gridX, int gridY) {
        if (entity instanceof ConveyorBelt belt) {
            placeConveyor(gridX, gridY, null);
            return true;
        }
        return false;
    }
}