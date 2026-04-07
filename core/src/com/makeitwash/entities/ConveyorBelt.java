package com.makeitwash.entities;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.makeitwash.world.Grid;

/**
 * Nastro trasportatore con texture generate proceduralmente (pixel art).
 *
 * ── SISTEMA DI COORDINATE ──────────────────────────────────────────────────
 * LibGDX world space: Y verso l'ALTO.
 * Pixmap: Y verso il BASSO.
 * SpriteBatch.draw() con rotation: angoli CCW in world space.
 * Il Pixmap viene flippato su Y al rendering => a_pixmap = -a_world.
 *
 * ── ATLAS CURVO ────────────────────────────────────────────────────────────
 * Centro arco Pixmap: (0, 0) = angolo top-left
 *   0°  → (r, 0)  = bordo NORTH (top Pixmap)   [uscita]
 *   90° → (0, r)  = bordo WEST  (left Pixmap)   [ingresso]
 * Flusso atlas base: WEST ingresso → NORTH uscita
 *
 * Rotazioni SpriteBatch CCW:
 *   0°   → WEST→NORTH
 *   90°  → SOUTH→WEST
 *   180° → EAST→SOUTH
 *   270° → NORTH→EAST
 *
 * ── ALLINEAMENTO VISIVO ────────────────────────────────────────────────────
 * BELT_INNER = beltTop = 10   (stessa quota per curva e dritto)
 * BELT_OUTER = beltBot = 54   (stessa quota per curva e dritto)
 * Questo garantisce che la fascia del nastro sia continua ai giunti.
 *
 * ── SISTEMA FLUSSI ─────────────────────────────────────────────────────────
 * Ogni nastro ha inputDirection e outputDirection espliciti.
 * Al piazzamento, Grid.propagateFlow() fa BFS dalla sorgente per propagare
 * il flusso a tutti i nastri connessi nella catena.
 */
public class ConveyorBelt extends PlaceableEntity {

    // =========================================================================
    // ANIMAZIONE GLOBALE SINCRONIZZATA
    // =========================================================================
    private static float globalProgress = 0f;
    private static float globalSpeed    = 1.2f;

    public static void tickGlobal(float delta) {
        globalProgress = (globalProgress + delta * globalSpeed) % 1f;
    }
    public static void setGlobalSpeed(float s) { globalSpeed = s; }
    public static float getGlobalProgress()    { return globalProgress; }

    // =========================================================================
    // Direzioni
    // =========================================================================
    public enum Direction { NORTH, SOUTH, EAST, WEST;

        public Direction opposite() {
            switch (this) {
                case NORTH: return SOUTH;
                case SOUTH: return NORTH;
                case EAST:  return WEST;
                case WEST:  return EAST;
                default:    return EAST;
            }
        }

        /** Offset griglia nella direzione data (LibGDX Y up). */
        public int dx() {
            switch (this) { case EAST: return 1; case WEST: return -1; default: return 0; }
        }
        public int dy() {
            switch (this) { case NORTH: return 1; case SOUTH: return -1; default: return 0; }
        }
    }

    // =========================================================================
    // Stato flusso
    // =========================================================================
    /** Da dove arriva il nastro (direzione da cui entra il flusso). */
    private Direction inputDirection  = Direction.WEST;
    /** Dove va il flusso (direzione verso cui esce). */
    private Direction outputDirection = Direction.EAST;

    // Connessioni fisiche con i vicini
    private boolean connectedNorth = false;
    private boolean connectedSouth = false;
    private boolean connectedEast  = false;
    private boolean connectedWest  = false;
    private boolean curved         = false;

    // =========================================================================
    // Costanti texture
    // =========================================================================
    static final int TILE = 64;
    private static final int S_FRAMES = 8;
    private static final int C_FRAMES = 8;

    // Fascia nastro — STESSI valori per dritto e curvo per continuità visiva
    private static final int BELT_INNER = 10;  // = beltTop nastro dritto
    private static final int BELT_OUTER = 54;  // = beltBot nastro dritto
    private static final int BELT_MID   = (BELT_INNER + BELT_OUTER) / 2; // 32

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final int COL_BASE   = rgba(0.20f, 0.22f, 0.27f, 1f);
    private static final int COL_BELT   = rgba(0.27f, 0.30f, 0.35f, 1f);
    private static final int COL_EDGE   = rgba(0.12f, 0.13f, 0.16f, 1f);
    private static final int COL_STRIPE = rgba(0.44f, 0.48f, 0.55f, 1f);
    private static final int COL_ARROW  = rgba(0.70f, 0.76f, 0.85f, 1f);
    private static final int COL_SHADOW = rgba(0.09f, 0.10f, 0.12f, 1f);

    // =========================================================================
    // Texture statiche condivise
    // =========================================================================
    private static Texture       straightTex;
    private static Texture       curveTex;
    private static TextureRegion straightReg;
    private static TextureRegion curveReg;
    private static boolean       loaded = false;

    public static void ensureTexturesLoaded() {
        if (loaded) return;
        loaded      = true;
        straightTex = buildStraightAtlas();
        curveTex    = buildCurveAtlas();
        straightReg = new TextureRegion(straightTex, 0, 0, TILE, TILE);
        curveReg    = new TextureRegion(curveTex,    0, 0, TILE, TILE);
    }

    public static void disposeTextures() {
        if (straightTex != null) { straightTex.dispose(); straightTex = null; }
        if (curveTex    != null) { curveTex.dispose();    curveTex    = null; }
        straightReg = curveReg = null;
        loaded = false;
    }

    // =========================================================================
    // ATLAS NASTRO DRITTO
    // =========================================================================
    private static Texture buildStraightAtlas() {
        Pixmap p = new Pixmap(TILE * S_FRAMES, TILE, Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);

        final int beltTop    = BELT_INNER;
        final int beltBot    = BELT_OUTER;
        final int stripeStep = 10;
        final int stripeW    = 3;

        for (int f = 0; f < S_FRAMES; f++) {
            int ox = f * TILE;

            fillRect(p, ox, 0,        TILE, TILE, COL_BASE);
            fillRect(p, ox, 0,        TILE, 2,    COL_SHADOW);
            fillRect(p, ox, TILE - 2, TILE, 2,    COL_SHADOW);

            fillRect(p, ox, beltTop, TILE, beltBot - beltTop, COL_BELT);
            fillRect(p, ox, beltTop,     TILE, 3, COL_EDGE);
            fillRect(p, ox, beltBot - 3, TILE, 3, COL_EDGE);

            int offset = (f * TILE) / S_FRAMES;
            for (int row = beltTop + 3; row < beltBot - 3; row++) {
                int diagShift = offset + (row - beltTop) / 2;
                for (int s = -TILE; s < TILE * 2; s += stripeStep) {
                    for (int sw = 0; sw < stripeW; sw++) {
                        int drawX = ox + ((s + diagShift + sw) % TILE + TILE) % TILE;
                        if (drawX >= ox && drawX < ox + TILE)
                            p.drawPixel(drawX, row, COL_STRIPE);
                    }
                }
            }

            drawArrowH(p, ox + TILE / 2, TILE / 2, 12, COL_ARROW, beltTop + 4, beltBot - 4);
        }

        Texture t = new Texture(p);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        p.dispose();
        return t;
    }

    // =========================================================================
    // ATLAS NASTRO CURVO
    // =========================================================================
    /**
     * Centro Pixmap (0,0), arco 0°→90°.
     * BELT_INNER/BELT_OUTER identici al nastro dritto per allineamento perfetto.
     */
    private static Texture buildCurveAtlas() {
        Pixmap p = new Pixmap(TILE * C_FRAMES, TILE, Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);

        final double START_DEG = 0.0;
        final double END_DEG   = 90.0;
        final double TOTAL_ARC = END_DEG - START_DEG;
        final int    N_STRIPES = 6;

        for (int f = 0; f < C_FRAMES; f++) {
            int ox = f * TILE;

            fillRect(p, ox, 0, TILE, TILE, COL_BASE);
            fillRect(p, ox, 0, 2,    TILE, COL_SHADOW);
            fillRect(p, ox, 0, TILE, 2,    COL_SHADOW);

            // Fascia arco
            for (double deg = START_DEG; deg <= END_DEG; deg += 0.25) {
                double rad  = Math.toRadians(deg);
                double cosA = Math.cos(rad);
                double sinA = Math.sin(rad);
                for (int r = BELT_INNER; r <= BELT_OUTER; r++) {
                    int px = ox + (int) Math.round(r * cosA);
                    int py =      (int) Math.round(r * sinA);
                    if (px >= ox && px < ox + TILE && py >= 0 && py < TILE)
                        p.drawPixel(px, py, COL_BELT);
                }
            }

            // Bordi inner/outer
            drawArcLine(p, ox, 0, 0, BELT_INNER, 3, START_DEG, END_DEG, COL_EDGE);
            drawArcLine(p, ox, 0, 0, BELT_OUTER, 3, START_DEG, END_DEG, COL_EDGE);

            // Strisce animate
            double degPerStripe   = TOTAL_ARC / N_STRIPES;
            double frameOffsetDeg = ((double) f / C_FRAMES) * degPerStripe;

            for (int s = 0; s < N_STRIPES + 1; s++) {
                double sd = START_DEG + s * degPerStripe - frameOffsetDeg;
                double ed = sd + 3.5;
                if (ed < START_DEG || sd > END_DEG) continue;
                sd = Math.max(sd, START_DEG);
                ed = Math.min(ed, END_DEG);
                drawArcLine(p, ox, 0, 0, BELT_MID, 6, sd, ed, COL_STRIPE);
            }

            // Freccia a 45° (metà arco), tangente verso uscita NORTH
            double midRad = Math.toRadians(45.0);
            int arrowX = ox + (int)(BELT_MID * Math.cos(midRad));
            int arrowY =      (int)(BELT_MID * Math.sin(midRad));
            double tanDeg = 45.0 - 90.0; // tangente CW verso uscita
            double tx = Math.cos(Math.toRadians(tanDeg));
            double ty = Math.sin(Math.toRadians(tanDeg));
            drawArrowPixmap(p, arrowX, arrowY, tx, ty, 7, COL_ARROW, ox, TILE);

            // Continuità bordi — stessa posizione della fascia del dritto
            // NORTH (py=0): allinea con beltTop del nastro dritto sopra
            fillRect(p, ox + BELT_INNER, 0, BELT_OUTER - BELT_INNER, 2, COL_EDGE);
            // WEST (px=ox): allinea con beltTop del nastro dritto a sinistra
            fillRect(p, ox, BELT_INNER, 2, BELT_OUTER - BELT_INNER, COL_EDGE);
        }

        Texture t = new Texture(p);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        p.dispose();
        return t;
    }

    // =========================================================================
    // Costruttori
    // =========================================================================
    public ConveyorBelt() {
        super();
        ensureTexturesLoaded();
    }

    public ConveyorBelt(boolean curved) {
        super();
        ensureTexturesLoaded();
        this.curved = curved;
    }

    public ConveyorBelt(int gridX, int gridY) {
        super();
        ensureTexturesLoaded();
        setGridPosition(gridX, gridY);
    }

    // =========================================================================
    // Connessioni fisiche
    // =========================================================================
    public void updateConnections(Grid grid) {
        connectedNorth = grid.hasConveyorAt(gridX, gridY + 1);
        connectedSouth = grid.hasConveyorAt(gridX, gridY - 1);
        connectedEast  = grid.hasConveyorAt(gridX + 1, gridY);
        connectedWest  = grid.hasConveyorAt(gridX - 1, gridY);
    }

    // =========================================================================
    // SISTEMA FLUSSI
    // =========================================================================

    /**
     * Imposta la direzione di ingresso e calcola automaticamente quella di uscita.
     * Per i nastri dritti: uscita = opposto dell'ingresso (scorrimento lineare).
     * Per le curve: uscita = la connessione disponibile che NON è l'ingresso.
     *
     * @param from direzione da cui arriva il flusso (es. WEST = il flusso viene da ovest)
     */
    public void applyFlow(Direction from) {
        this.inputDirection = from;
        if (isCurveConnection()) {
            // Uscita = l'unica connessione diversa dall'ingresso
            Direction incomingFrom = from.opposite(); // lato fisico da cui entra
            for (Direction d : Direction.values()) {
                if (isConnectedIn(d) && d != incomingFrom) {
                    this.outputDirection = d;
                    return;
                }
            }
        } else {
            // Nastro dritto: uscita opposta all'ingresso
            this.outputDirection = from.opposite();
        }
    }

    /** @return true se questo nastro ha un vicino fisico nella direzione d */
    private boolean isConnectedIn(Direction d) {
        switch (d) {
            case NORTH: return connectedNorth;
            case SOUTH: return connectedSouth;
            case EAST:  return connectedEast;
            case WEST:  return connectedWest;
            default:    return false;
        }
    }

    /**
     * Restituisce la cella successiva nella catena del flusso.
     * Usato da Grid.propagateFlow() e dal movimento degli item.
     */
    public int nextGridX() { return gridX + outputDirection.dx(); }
    public int nextGridY() { return gridY + outputDirection.dy(); }

    // =========================================================================
    // Update / Render
    // =========================================================================
    @Override
    public void update(float delta) { /* tick globale gestito da Grid */ }

    @Override
    public void render(SpriteBatch batch) {
        if (straightTex == null) return;

        boolean curve   = isCurveConnection();
        int totalFrames = curve ? C_FRAMES : S_FRAMES;
        int frame       = (int)(globalProgress * totalFrames) % totalFrames;
        float rotation  = getRotation();
        float cx        = TILE / 2f;
        float cy        = TILE / 2f;

        if (curve) {
            curveReg.setRegionX(frame * TILE);
            curveReg.setRegionWidth(TILE);
            batch.draw(curveReg, x, y, cx, cy, TILE, TILE, 1f, 1f, rotation);
        } else {
            straightReg.setRegionX(frame * TILE);
            straightReg.setRegionWidth(TILE);
            batch.draw(straightReg, x, y, cx, cy, TILE, TILE, 1f, 1f, rotation);
        }
    }

    // =========================================================================
    // Rotazione visiva
    // =========================================================================
    /**
     * NASTRI DRITTI — basati su outputDirection:
     *   0°   → EAST   90°  → NORTH   180° → WEST   270° → SOUTH
     *
     * NASTRI CURVI — atlas base: WEST ingresso, NORTH uscita
     *   Rotazione derivata dalla coppia (input, output) del flusso reale:
     *   WEST→NORTH   0°     SOUTH→WEST  90°
     *   EAST→SOUTH  180°   NORTH→EAST  270°
     */
    private float getRotation() {
        if (isCurveConnection()) {
            Direction in  = inputDirection.opposite(); // lato fisico ingresso
            Direction out = outputDirection;
            // Confronta con le 4 varianti dell'atlas
            if (in == Direction.WEST  && out == Direction.NORTH) return   0f;
            if (in == Direction.SOUTH && out == Direction.WEST)  return  90f;
            if (in == Direction.EAST  && out == Direction.SOUTH) return 180f;
            if (in == Direction.NORTH && out == Direction.EAST)  return 270f;
            // Fallback: usa connessioni fisiche
            if (connectedWest  && connectedNorth) return   0f;
            if (connectedSouth && connectedWest)  return  90f;
            if (connectedEast  && connectedSouth) return 180f;
            if (connectedNorth && connectedEast)  return 270f;
            return 0f;
        }
        switch (outputDirection) {
            case EAST:  return   0f;
            case NORTH: return  90f;
            case WEST:  return 180f;
            case SOUTH: return 270f;
            default:    return   0f;
        }
    }

    // =========================================================================
    // Helpers geometria
    // =========================================================================
    public boolean isCurveConnection() {
        if (countConnections() != 2) return false;
        return !((connectedNorth && connectedSouth) || (connectedEast && connectedWest));
    }

    private int countConnections() {
        int c = 0;
        if (connectedNorth) c++;
        if (connectedSouth) c++;
        if (connectedEast)  c++;
        if (connectedWest)  c++;
        return c;
    }

    private static void drawArcLine(Pixmap p, int ox, double cx, double cy,
                                    int radius, int thickness,
                                    double startDeg, double endDeg, int color) {
        p.setColor(color);
        int half = thickness / 2;
        double step = Math.max(0.2, Math.toDegrees(0.5 / radius));
        for (double deg = startDeg; deg <= endDeg; deg += step) {
            double rad  = Math.toRadians(deg);
            double cosA = Math.cos(rad);
            double sinA = Math.sin(rad);
            for (int t = -half; t <= half; t++) {
                int px = ox + (int) Math.round(cx + (radius + t) * cosA);
                int py =      (int) Math.round(cy + (radius + t) * sinA);
                if (px >= ox && px < ox + TILE && py >= 0 && py < TILE)
                    p.drawPixel(px, py);
            }
        }
    }

    private static void drawArrowH(Pixmap p, int cx, int cy, int len,
                                   int color, int clipTop, int clipBot) {
        for (int ax = cx - len; ax <= cx + len / 3; ax++)
            for (int ay = cy - 1; ay <= cy + 1; ay++)
                if (ay >= clipTop && ay < clipBot) p.drawPixel(ax, ay, color);
        for (int tip = 0; tip < 7; tip++) {
            int tx = cx + len / 3 + tip;
            for (int dy = -tip; dy <= tip; dy++) {
                int ay = cy + dy;
                if (ay >= clipTop && ay < clipBot) p.drawPixel(tx, ay, color);
            }
        }
    }

    private static void drawArrowPixmap(Pixmap p, int cx, int cy,
                                        double tx, double ty, int len,
                                        int color, int ox, int tileSize) {
        for (int i = -len; i <= len; i++) {
            int px = ox + cx + (int)(tx * i);
            int py =      cy + (int)(ty * i);
            if (px >= ox && px < ox + tileSize && py >= 0 && py < tileSize)
                p.drawPixel(px, py, color);
        }
        for (int tip = 1; tip <= 5; tip++) {
            int bx = ox + cx + (int)(tx * len) + (int)(-ty * tip);
            int by =      cy + (int)(ty * len) + (int)( tx * tip);
            if (bx >= ox && bx < ox + tileSize && by >= 0 && by < tileSize)
                p.drawPixel(bx, by, color);
            bx = ox + cx + (int)(tx * len) + (int)(ty * tip);
            by =      cy + (int)(ty * len) + (int)(-tx * tip);
            if (bx >= ox && bx < ox + tileSize && by >= 0 && by < tileSize)
                p.drawPixel(bx, by, color);
        }
    }

    private static void fillRect(Pixmap p, int x, int y, int w, int h, int color) {
        p.setColor(color);
        p.fillRectangle(x, y, w, h);
    }

    private static int rgba(float r, float g, float b, float a) {
        return (((int)(r * 255) & 0xFF) << 24)
             | (((int)(g * 255) & 0xFF) << 16)
             | (((int)(b * 255) & 0xFF) <<  8)
             |  ((int)(a * 255) & 0xFF);
    }

    // =========================================================================
    // Getters
    // =========================================================================
    public Direction getInputDirection()  { return inputDirection; }
    public Direction getOutputDirection() { return outputDirection; }
    public boolean isConnectedNorth()     { return connectedNorth; }
    public boolean isConnectedSouth()     { return connectedSouth; }
    public boolean isConnectedEast()      { return connectedEast; }
    public boolean isConnectedWest()      { return connectedWest; }
}