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
 * Pixmap: Y verso il BASSO (coordinate "screen").
 * SpriteBatch.draw() con rotation: angoli in gradi CCW in world space.
 * Il Pixmap viene flippato automaticamente su Y al momento del rendering.
 *
 * Trasformazione Pixmap → World:
 *   world_x = px
 *   world_y = TILE - 1 - py
 * Quindi un angolo in Pixmap-space a_p corrisponde a world angle a_w = -a_p.
 *
 * ── ATLAS CURVO ────────────────────────────────────────────────────────────
 * Centro arco in Pixmap: (0, 0) = angolo top-left
 *   0°  in Pixmap → punto (r, 0)  = bordo NORTH del tile (top Pixmap = top World)
 *   90° in Pixmap → punto (0, r)  = bordo WEST  del tile (left Pixmap = left World)
 * → Flusso atlas base: WEST ingresso, NORTH uscita
 *
 * Rotazioni SpriteBatch (CCW in world):
 *   0°   → WEST→NORTH
 *   90°  → SOUTH→WEST
 *   180° → EAST→SOUTH
 *   270° → NORTH→EAST
 */
public class ConveyorBelt extends PlaceableEntity {

    // =========================================================================
    // ANIMAZIONE GLOBALE SINCRONIZZATA
    // =========================================================================
    private static float globalProgress = 0f;
    private static float globalSpeed    = 1.2f; // cicli/secondo

    public static void tickGlobal(float delta) {
        globalProgress = (globalProgress + delta * globalSpeed) % 1f;
    }
    public static void setGlobalSpeed(float s)  { globalSpeed = s; }
    public static float getGlobalProgress()     { return globalProgress; }

    // =========================================================================
    // Stato connessioni
    // =========================================================================
    private boolean   curved          = false;
    private boolean   connectedNorth  = false;
    private boolean   connectedSouth  = false;
    private boolean   connectedEast   = false;
    private boolean   connectedWest   = false;
    private Direction outputDirection = Direction.EAST;

    public enum Direction { NORTH, SOUTH, EAST, WEST }

    // =========================================================================
    // Costanti texture
    // =========================================================================
    static final int TILE      = 64;
    private static final int S_FRAMES = 8;
    private static final int C_FRAMES = 8;

    // Fascia nastro (pixel del bordo al tile, inner/outer radius)
    private static final int BELT_INNER = 16;
    private static final int BELT_OUTER = 48;
    private static final int BELT_MID   = (BELT_INNER + BELT_OUTER) / 2;

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final int COL_BASE   = rgba(0.20f, 0.22f, 0.27f, 1f); // sfondo tile
    private static final int COL_BELT   = rgba(0.27f, 0.30f, 0.35f, 1f); // fascia nastro
    private static final int COL_EDGE   = rgba(0.12f, 0.13f, 0.16f, 1f); // bordi nastro
    private static final int COL_STRIPE = rgba(0.44f, 0.48f, 0.55f, 1f); // strisce animate
    private static final int COL_ARROW  = rgba(0.70f, 0.76f, 0.85f, 1f); // freccia
    private static final int COL_SHADOW = rgba(0.09f, 0.10f, 0.12f, 1f); // ombra bordo tile

    // =========================================================================
    // Texture (statiche, condivise tra tutte le istanze)
    // =========================================================================
    private static Texture       straightTex;
    private static Texture       curveTex;
    private static TextureRegion straightReg;
    private static TextureRegion curveReg;
    private static boolean       loaded = false;

    public static void ensureTexturesLoaded() {
        if (loaded) return;
        loaded     = true;
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
    /**
     * S_FRAMES tile affiancati orizzontalmente, orientati EAST (→).
     * Le strisce diagonali si spostano di TILE/S_FRAMES pixel per frame.
     */
    private static Texture buildStraightAtlas() {
        Pixmap p = new Pixmap(TILE * S_FRAMES, TILE, Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);

        final int beltTop    = 10;
        final int beltBot    = TILE - 10;
        final int stripeStep = 10;
        final int stripeW    = 3;

        for (int f = 0; f < S_FRAMES; f++) {
            int ox = f * TILE;

            // Sfondo + ombre bordi tile
            fillRect(p, ox, 0,        TILE, TILE, COL_BASE);
            fillRect(p, ox, 0,        TILE, 2,    COL_SHADOW);
            fillRect(p, ox, TILE - 2, TILE, 2,    COL_SHADOW);

            // Fascia
            fillRect(p, ox, beltTop, TILE, beltBot - beltTop, COL_BELT);

            // Bordi nastro
            fillRect(p, ox, beltTop,     TILE, 3, COL_EDGE);
            fillRect(p, ox, beltBot - 3, TILE, 3, COL_EDGE);

            // Strisce diagonali animate
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

            // Freccia →
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
     * Geometria (coordinate Pixmap, Y verso il basso):
     *   Centro arco: (0, 0) = angolo top-left del tile
     *   Arco: 0° → 90°
     *     0°  → (r, 0)  = bordo superiore (NORTH in world)   [uscita]
     *     90° → (0, r)  = bordo sinistro  (WEST  in world)   [ingresso]
     *
     * Dopo il flip Y di LibGDX:
     *   world NORTH = py=0      → uscita verso il nastro sopra  ✓
     *   world WEST  = px=0      → ingresso dal nastro a sinistra ✓
     *   Flusso: WEST → NORTH
     */
    private static Texture buildCurveAtlas() {
        Pixmap p = new Pixmap(TILE * C_FRAMES, TILE, Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);

        final double START_DEG  =  0.0;
        final double END_DEG    = 90.0;
        final double TOTAL_ARC  = END_DEG - START_DEG;
        final int    N_STRIPES  = 6;

        for (int f = 0; f < C_FRAMES; f++) {
            int ox = f * TILE;

            // Sfondo + ombre
            fillRect(p, ox, 0, TILE, TILE, COL_BASE);
            fillRect(p, ox, 0, 2,    TILE, COL_SHADOW);
            fillRect(p, ox, 0, TILE, 2,    COL_SHADOW);

            // ── Fascia dell'arco ─────────────────────────────────────────────
            for (double deg = START_DEG; deg <= END_DEG; deg += 0.25) {
                double rad  = Math.toRadians(deg);
                double cosA = Math.cos(rad);
                double sinA = Math.sin(rad);
                for (int r = BELT_INNER; r <= BELT_OUTER; r++) {
                    int px = ox + (int)Math.round(r * cosA); // cx=0
                    int py =      (int)Math.round(r * sinA); // cy=0
                    if (px >= ox && px < ox + TILE && py >= 0 && py < TILE)
                        p.drawPixel(px, py, COL_BELT);
                }
            }

            // ── Bordi inner / outer ──────────────────────────────────────────
            drawArcLine(p, ox, 0, 0, BELT_INNER, 3, START_DEG, END_DEG, COL_EDGE);
            drawArcLine(p, ox, 0, 0, BELT_OUTER, 3, START_DEG, END_DEG, COL_EDGE);

            // ── Strisce animate ──────────────────────────────────────────────
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

            // ── Freccia a metà arco (45°), tangente al moto ──────────────────
            double midRad = Math.toRadians(45.0);
            int arrowX = ox + (int)(BELT_MID * Math.cos(midRad));
            int arrowY =      (int)(BELT_MID * Math.sin(midRad));
            // Tangente CCW nella direzione del flusso (da 90° verso 0°, cioè CW in Pixmap)
            // Il flusso va da 90° a 0° (WEST→NORTH), quindi tangente a 45° punta verso ~-45°
            double tanDeg = 45.0 - 90.0; // = -45°
            double tx = Math.cos(Math.toRadians(tanDeg));
            double ty = Math.sin(Math.toRadians(tanDeg));
            drawArrowPixmap(p, arrowX, arrowY, tx, ty, 7, COL_ARROW, ox, TILE);

            // ── Continuità bordi per allineamento visivo ─────────────────────
            // Bordo NORTH (py=0, top del Pixmap): striscia orizzontale
            fillRect(p, ox + BELT_INNER, 0, BELT_OUTER - BELT_INNER, 2, COL_EDGE);
            // Bordo WEST (px=ox, sinistra del tile): striscia verticale
            fillRect(p, ox, BELT_INNER, 2, BELT_OUTER - BELT_INNER, COL_EDGE);
        }

        Texture t = new Texture(p);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        p.dispose();
        return t;
    }

    // =========================================================================
    // Helper: disegna una linea ad arco (anello)
    // =========================================================================
    private static void drawArcLine(Pixmap p, int ox,
                                    double cx, double cy,
                                    int radius, int thickness,
                                    double startDeg, double endDeg,
                                    int color) {
        p.setColor(color);
        int half = thickness / 2;
        double step = Math.max(0.2, Math.toDegrees(0.5 / radius)); // passo adattivo
        for (double deg = startDeg; deg <= endDeg; deg += step) {
            double rad  = Math.toRadians(deg);
            double cosA = Math.cos(rad);
            double sinA = Math.sin(rad);
            for (int t = -half; t <= half; t++) {
                int px = ox + (int)Math.round(cx + (radius + t) * cosA);
                int py =      (int)Math.round(cy + (radius + t) * sinA);
                if (px >= ox && px < ox + TILE && py >= 0 && py < TILE)
                    p.drawPixel(px, py);
            }
        }
    }

    // =========================================================================
    // Helper: disegna freccia orizzontale (per nastro dritto)
    // =========================================================================
    private static void drawArrowH(Pixmap p, int cx, int cy, int len,
                                   int color, int clipTop, int clipBot) {
        // Corpo
        for (int ax = cx - len; ax <= cx + len / 3; ax++)
            for (int ay = cy - 1; ay <= cy + 1; ay++)
                if (ay >= clipTop && ay < clipBot) p.drawPixel(ax, ay, color);
        // Punta
        for (int tip = 0; tip < 7; tip++) {
            int tx = cx + len / 3 + tip;
            for (int dy = -tip; dy <= tip; dy++) {
                int ay = cy + dy;
                if (ay >= clipTop && ay < clipBot) p.drawPixel(tx, ay, color);
            }
        }
    }

    // =========================================================================
    // Helper: disegna freccia direzionale su Pixmap
    // =========================================================================
    private static void drawArrowPixmap(Pixmap p, int cx, int cy,
                                        double tx, double ty, int len,
                                        int color, int ox, int tileSize) {
        // Corpo
        for (int i = -len; i <= len; i++) {
            int px = ox + cx + (int)(tx * i);
            int py =      cy + (int)(ty * i);
            if (px >= ox && px < ox + tileSize && py >= 0 && py < tileSize)
                p.drawPixel(px, py, color);
        }
        // Punta (triangolino)
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

    // =========================================================================
    // Helper: colore RGBA packed
    // =========================================================================
    private static int rgba(float r, float g, float b, float a) {
        return (((int)(r * 255) & 0xFF) << 24)
             | (((int)(g * 255) & 0xFF) << 16)
             | (((int)(b * 255) & 0xFF) <<  8)
             |  ((int)(a * 255) & 0xFF);
    }

    private static void fillRect(Pixmap p, int x, int y, int w, int h, int color) {
        p.setColor(color);
        p.fillRectangle(x, y, w, h);
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
    // Connessioni
    // =========================================================================
    public void updateConnections(Grid grid) {
        updateConnections(grid, null);
    }

    public void updateConnections(Grid grid, Direction fromDirection) {
        connectedNorth = grid.hasConveyorAt(gridX, gridY + 1);
        connectedSouth = grid.hasConveyorAt(gridX, gridY - 1);
        connectedEast  = grid.hasConveyorAt(gridX + 1, gridY);
        connectedWest  = grid.hasConveyorAt(gridX - 1, gridY);
        this.curved    = isCurveConnection();
        this.outputDirection = inferOutputDirection(fromDirection);
    }

    private Direction inferOutputDirection(Direction from) {
        int count = countConnections();
        if (count == 0) return Direction.EAST;
        if (count == 1) {
            if (connectedEast)  return Direction.EAST;
            if (connectedNorth) return Direction.NORTH;
            if (connectedWest)  return Direction.WEST;
            return Direction.SOUTH;
        }
        if (count == 2 && from != null) {
            Direction opp = opposite(from);
            if (connectedEast  && opp != Direction.EAST)  return Direction.EAST;
            if (connectedNorth && opp != Direction.NORTH) return Direction.NORTH;
            if (connectedWest  && opp != Direction.WEST)  return Direction.WEST;
            if (connectedSouth && opp != Direction.SOUTH) return Direction.SOUTH;
        }
        if (connectedEast)  return Direction.EAST;
        if (connectedNorth) return Direction.NORTH;
        if (connectedSouth) return Direction.SOUTH;
        return Direction.WEST;
    }

    private static Direction opposite(Direction d) {
        switch (d) {
            case NORTH: return Direction.SOUTH;
            case SOUTH: return Direction.NORTH;
            case EAST:  return Direction.WEST;
            case WEST:  return Direction.EAST;
            default:    return Direction.EAST;
        }
    }

    // =========================================================================
    // Update / Render
    // =========================================================================
    @Override
    public void update(float delta) { /* tick globale gestito da Grid */ }

    @Override
    public void render(SpriteBatch batch) {
        if (straightTex == null) return;

        boolean curve  = isCurveConnection();
        int totalFrames = curve ? C_FRAMES : S_FRAMES;
        int frame = (int)(globalProgress * totalFrames) % totalFrames;

        float rotation = getRotation();
        float cx = TILE / 2f;
        float cy = TILE / 2f;

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
     * NASTRI DRITTI:
     *   0°   → EAST  (atlas base, strisce → destra)
     *   90°  → NORTH
     *   180° → WEST
     *   270° → SOUTH
     *
     * NASTRI CURVI (atlas base: ingresso WEST, uscita NORTH):
     *   0°   → WEST→NORTH
     *   90°  → SOUTH→WEST
     *   180° → EAST→SOUTH
     *   270° → NORTH→EAST
     *
     * Nota: SpriteBatch.draw() ruota CCW in world space (Y up).
     * Il flip Y del Pixmap è già incluso nel sistema di coordinate dell'atlas.
     */
    private float getRotation() {
        if (isCurveConnection()) {
            // Ogni combinazione corrisponde a una e una sola rotazione
            if (connectedWest  && connectedNorth) return   0f;
            if (connectedSouth && connectedWest)  return  90f;
            if (connectedEast  && connectedSouth) return 180f;
            if (connectedNorth && connectedEast)  return 270f;
            // Fallback per connessioni incomplete
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
    // Helpers
    // =========================================================================
    public boolean isCurveConnection() {
        if (countConnections() != 2) return false;
        boolean straight = (connectedNorth && connectedSouth)
                        || (connectedEast  && connectedWest);
        return !straight;
    }

    private int countConnections() {
        int c = 0;
        if (connectedNorth) c++;
        if (connectedSouth) c++;
        if (connectedEast)  c++;
        if (connectedWest)  c++;
        return c;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public Direction getOutputDirection()  { return outputDirection; }
    public boolean   isConnectedNorth()    { return connectedNorth; }
    public boolean   isConnectedSouth()    { return connectedSouth; }
    public boolean   isConnectedEast()     { return connectedEast; }
    public boolean   isConnectedWest()     { return connectedWest; }
}