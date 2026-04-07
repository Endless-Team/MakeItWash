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
 * SpriteBatch rotation: CCW in world space. Flip Y implicito => a_pixmap = -a_world.
 *
 * ── NASTRO CURVO — FORMA DIAGONALE ────────────────────────────────────────
 * Il tile è tagliato dalla diagonale bottom-left → top-right.
 * Il triangolo in basso a destra (lato EAST/SOUTH) contiene le strisce
 * animate nella direzione di uscita.
 * Il triangolo in alto a sinistra (lato WEST/NORTH) ha sfondo scuro neutro.
 *
 * Atlas base: ingresso WEST (sinistra), uscita SOUTH (basso).
 *   - Triangolo attivo: angolo bottom-left → bottom-right → top-right
 *   - Strisce inclinate verso il basso-destra (direzione uscita SOUTH)
 *
 * Rotazioni SpriteBatch CCW:
 *   0°   → WEST→SOUTH    (diagonale ↗, flusso verso il basso)
 *   90°  → SOUTH→EAST    (diagonale ↗, flusso verso destra)
 *   180° → EAST→NORTH    (diagonale ↗, flusso verso l'alto)
 *   270° → NORTH→WEST    (diagonale ↗, flusso verso sinistra)
 *
 * ── ALLINEAMENTO ──────────────────────────────────────────────────────────
 * BELT_INNER = beltTop = 10, BELT_OUTER = beltBot = 54 (identici al dritto).
 */
public class ConveyorBelt extends PlaceableEntity {

    // =========================================================================
    // ANIMAZIONE GLOBALE
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
    public enum Direction {
        NORTH, SOUTH, EAST, WEST;

        public Direction opposite() {
            switch (this) {
                case NORTH: return SOUTH;  case SOUTH: return NORTH;
                case EAST:  return WEST;   case WEST:  return EAST;
                default: return EAST;
            }
        }
        public int dx() { return this == EAST ? 1 : this == WEST ? -1 : 0; }
        public int dy() { return this == NORTH ? 1 : this == SOUTH ? -1 : 0; }
    }

    // =========================================================================
    // Stato flusso
    // =========================================================================
    private Direction inputDirection  = Direction.WEST;
    private Direction outputDirection = Direction.EAST;

    private boolean connectedNorth = false;
    private boolean connectedSouth = false;
    private boolean connectedEast  = false;
    private boolean connectedWest  = false;

    // =========================================================================
    // Costanti texture
    // =========================================================================
    public static final int TILE     = 64;
    private static final int S_FRAMES = 8;
    private static final int C_FRAMES = 8;

    private static final int BELT_INNER = 10;
    private static final int BELT_OUTER = 54;

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final int COL_BASE   = rgba(0.20f, 0.22f, 0.27f, 1f);
    private static final int COL_BELT   = rgba(0.27f, 0.30f, 0.35f, 1f);
    private static final int COL_DARK   = rgba(0.14f, 0.15f, 0.19f, 1f); // triangolo inattivo
    private static final int COL_EDGE   = rgba(0.12f, 0.13f, 0.16f, 1f);
    private static final int COL_DIAG   = rgba(0.09f, 0.10f, 0.12f, 1f); // linea diagonale
    private static final int COL_STRIPE = rgba(0.44f, 0.48f, 0.55f, 1f);
    private static final int COL_ARROW  = rgba(0.70f, 0.76f, 0.85f, 1f);
    private static final int COL_SHADOW = rgba(0.09f, 0.10f, 0.12f, 1f);

    // =========================================================================
    // Texture statiche
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
    // ATLAS NASTRO CURVO — forma diagonale
    // =========================================================================
    /**
     * Il tile è diviso dalla diagonale che va dall'angolo bottom-left (ox, TILE)
     * all'angolo top-right (ox+TILE, 0) in coordinate Pixmap (Y down).
     *
     * Triangolo ATTIVO (basso-destra, sotto la diagonale):
     *   vertici Pixmap: (ox, TILE), (ox+TILE, TILE), (ox+TILE, 0)
     *   In world space (Y up): bottom-left, bottom-right, top-right
     *   Contiene la fascia del nastro con strisce animate.
     *   Bordi di continuità:
     *     - Lato inferiore (SOUTH in world = py=TILE in Pixmap): beltTop..beltOuter = BELT_INNER..BELT_OUTER
     *     - Lato destro    (EAST  in world = px=ox+TILE in Pixmap): idem
     *
     * Triangolo INATTIVO (alto-sinistra, sopra la diagonale): sfondo scuro.
     *
     * Atlas base: flusso WEST → SOUTH
     *   (ingresso dal lato sinistro in world = px=ox in Pixmap, nella banda BELT_INNER..BELT_OUTER)
     *   (uscita dal lato inferiore in world = py=TILE in Pixmap, nella banda BELT_INNER..BELT_OUTER)
     */
    private static Texture buildCurveAtlas() {
        Pixmap p = new Pixmap(TILE * C_FRAMES, TILE, Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);

        for (int f = 0; f < C_FRAMES; f++) {
            int ox = f * TILE;

            // 1. Sfondo base intero tile
            fillRect(p, ox, 0, TILE, TILE, COL_BASE);

            // 2. Triangolo inattivo (alto-sinistra): riempi con colore scuro
            //    I pixel sopra la diagonale: per ogni riga py, la diagonale è a px = ox + (TILE-1-py)
            //    Pixels inattivi: px < ox + (TILE - 1 - py)
            for (int py = 0; py < TILE; py++) {
                int diagX = TILE - 1 - py; // x relativa al tile della diagonale
                for (int px = 0; px < diagX; px++) {
                    p.drawPixel(ox + px, py, COL_DARK);
                }
            }

            // 3. Linea diagonale (spessa 3px)
            for (int i = -1; i <= 1; i++) {
                for (int py = 0; py < TILE; py++) {
                    int px = ox + (TILE - 1 - py) + i;
                    if (px >= ox && px < ox + TILE)
                        p.drawPixel(px, py, COL_DIAG);
                }
            }

            // 4. Fascia nastro nel triangolo attivo
            //    La fascia occupa le bande BELT_INNER..BELT_OUTER sia sul lato SOUTH (py=TILE-1)
            //    che sul lato EAST (px=ox+TILE-1).
            //    Riempiamo i pixel del triangolo attivo che rientrano nella "banda" del nastro.
            //    La banda è definita dalla distanza dal bordo:
            //    - distanza dal bordo SOUTH (py=TILE): TILE - 1 - py
            //    - distanza dal bordo EAST  (px=ox+TILE): TILE - 1 - (px-ox)
            //    Un pixel è nella fascia se almeno una delle due distanze è in [BELT_INNER, BELT_OUTER].
            for (int py = 0; py < TILE; py++) {
                int diagX = TILE - 1 - py;
                for (int relX = diagX; relX < TILE; relX++) {
                    int px = ox + relX;
                    int distSouth = TILE - 1 - py;
                    int distEast  = TILE - 1 - relX;
                    boolean inBandSouth = distSouth >= BELT_INNER && distSouth <= BELT_OUTER;
                    boolean inBandEast  = distEast  >= BELT_INNER && distEast  <= BELT_OUTER;
                    if (inBandSouth || inBandEast) {
                        p.drawPixel(px, py, COL_BELT);
                    }
                }
            }

            // 5. Bordi della fascia
            // Bordo SOUTH outer (py = TILE - 1 - BELT_OUTER)
            int southOuter = TILE - 1 - BELT_OUTER;
            int southInner = TILE - 1 - BELT_INNER;
            // Bordo EAST outer (relX = TILE - 1 - BELT_OUTER)
            int eastOuter = TILE - 1 - BELT_OUTER;
            int eastInner = TILE - 1 - BELT_INNER;

            // Linee bordo sul lato SOUTH (riga inferiore del tile)
            for (int bw = 0; bw < 3; bw++) {
                int py = TILE - 1 - BELT_INNER + bw;
                if (py >= 0 && py < TILE)
                    for (int relX = eastOuter; relX < TILE; relX++)
                        p.drawPixel(ox + relX, py, COL_EDGE);
                py = TILE - 1 - BELT_OUTER - bw;
                if (py >= 0 && py < TILE)
                    for (int relX = eastOuter; relX < TILE; relX++)
                        if (relX >= TILE - 1 - py) // solo nel triangolo attivo
                            p.drawPixel(ox + relX, py, COL_EDGE);
            }
            // Linee bordo sul lato EAST (colonna destra del tile)
            for (int bw = 0; bw < 3; bw++) {
                int relX = TILE - 1 - BELT_INNER + bw;
                if (relX >= 0 && relX < TILE)
                    for (int py = southOuter; py < TILE; py++)
                        p.drawPixel(ox + relX, py, COL_EDGE);
                relX = TILE - 1 - BELT_OUTER - bw;
                if (relX >= 0 && relX < TILE)
                    for (int py = southOuter; py < TILE; py++)
                        if (relX >= TILE - 1 - py) // solo nel triangolo attivo
                            p.drawPixel(ox + relX, py, COL_EDGE);
            }

            // 6. Strisce animate nella fascia
            //    Le strisce scorrono diagonalmente nella fascia del triangolo.
            //    Offset per frame: strisce si spostano nella direzione del flusso.
            int stripeStep = 10;
            int stripeW    = 3;
            int offset     = (f * stripeStep * C_FRAMES) / C_FRAMES; // scorrimento per frame
            offset = (f * TILE) / C_FRAMES;

            for (int py = 0; py < TILE; py++) {
                int diagX = TILE - 1 - py;
                int distSouth = TILE - 1 - py;
                for (int relX = diagX + 1; relX < TILE; relX++) {
                    int distEast = TILE - 1 - relX;
                    boolean inBand = (distSouth >= BELT_INNER + 3 && distSouth <= BELT_OUTER - 3)
                                  || (distEast  >= BELT_INNER + 3 && distEast  <= BELT_OUTER - 3);
                    if (!inBand) continue;
                    // Coordinata lungo la diagonale della banda (somma costante per linee a 45°)
                    int diagCoord = (relX + py + offset) % stripeStep;
                    if (diagCoord >= 0 && diagCoord < stripeW)
                        p.drawPixel(ox + relX, py, COL_STRIPE);
                }
            }

            // 7. Freccia nel triangolo attivo (direzione uscita: verso SOUTH = bottom)
            //    Centro approssimativo del triangolo attivo
            int arrowCX = ox + TILE * 2 / 3;
            int arrowCY = TILE * 2 / 3;
            // Freccia punta verso il basso (uscita SOUTH in world = py crescente in Pixmap)
            drawArrowV(p, arrowCX, arrowCY, 8, COL_ARROW, ox, TILE);

            // 8. Ombre bordi tile
            fillRect(p, ox, 0,        TILE, 2, COL_SHADOW);
            fillRect(p, ox, TILE - 2, TILE, 2, COL_SHADOW);
        }

        Texture t = new Texture(p);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        p.dispose();
        return t;
    }

    // =========================================================================
    // Costruttori
    // =========================================================================
    public ConveyorBelt() { super(); ensureTexturesLoaded(); }
    public ConveyorBelt(boolean curved) { super(); ensureTexturesLoaded(); }
    public ConveyorBelt(int gridX, int gridY) {
        super(); ensureTexturesLoaded(); setGridPosition(gridX, gridY);
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
    public void applyFlow(Direction from) {
        this.inputDirection = from;
        if (isCurveConnection()) {
            Direction incomingPhysical = from.opposite();
            for (Direction d : Direction.values()) {
                if (isConnectedIn(d) && d != incomingPhysical) {
                    this.outputDirection = d;
                    return;
                }
            }
        } else {
            this.outputDirection = from.opposite();
        }
    }

    private boolean isConnectedIn(Direction d) {
        switch (d) {
            case NORTH: return connectedNorth;
            case SOUTH: return connectedSouth;
            case EAST:  return connectedEast;
            case WEST:  return connectedWest;
            default:    return false;
        }
    }

    public int nextGridX() { return gridX + outputDirection.dx(); }
    public int nextGridY() { return gridY + outputDirection.dy(); }

    // =========================================================================
    // Update / Render
    // =========================================================================
    @Override
    public void update(float delta) {}

    @Override
    public void render(SpriteBatch batch) {
        if (straightTex == null) return;

        boolean curve   = isCurveConnection();
        int totalFrames = curve ? C_FRAMES : S_FRAMES;
        int frame       = (int)(globalProgress * totalFrames) % totalFrames;
        float rotation  = getRotation();

        if (curve) {
            curveReg.setRegionX(frame * TILE);
            curveReg.setRegionWidth(TILE);
            batch.draw(curveReg, x, y, TILE / 2f, TILE / 2f, TILE, TILE, 1f, 1f, rotation);
        } else {
            straightReg.setRegionX(frame * TILE);
            straightReg.setRegionWidth(TILE);
            batch.draw(straightReg, x, y, TILE / 2f, TILE / 2f, TILE, TILE, 1f, 1f, rotation);
        }
    }

    // =========================================================================
    // Rotazione visiva
    // =========================================================================
    /**
     * NASTRI DRITTI: basati su outputDirection.
     *   0° → EAST,  90° → NORTH,  180° → WEST,  270° → SOUTH
     *
     * NASTRI CURVI — atlas base: WEST ingresso, SOUTH uscita (triangolo in basso-destra)
     * Nota: in LibGDX SpriteBatch, rotation CCW. Con flip Y del Pixmap:
     *   0°   → ingresso WEST,  uscita SOUTH
     *   90°  → ingresso SOUTH, uscita EAST
     *   180° → ingresso EAST,  uscita NORTH
     *   270° → ingresso NORTH, uscita WEST
     */
    private float getRotation() {
        if (isCurveConnection()) {
            // Usa il flusso calcolato da applyFlow()
            Direction in  = inputDirection.opposite(); // lato fisico di ingresso
            Direction out = outputDirection;
            if (in == Direction.WEST  && out == Direction.SOUTH) return   0f;
            if (in == Direction.SOUTH && out == Direction.EAST)  return  90f;
            if (in == Direction.EAST  && out == Direction.NORTH) return 180f;
            if (in == Direction.NORTH && out == Direction.WEST)  return 270f;
            // Fallback fisico
            if (connectedWest  && connectedSouth) return   0f;
            if (connectedSouth && connectedEast)  return  90f;
            if (connectedEast  && connectedNorth) return 180f;
            if (connectedNorth && connectedWest)  return 270f;
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
        return !((connectedNorth && connectedSouth) || (connectedEast && connectedWest));
    }

    private int countConnections() {
        int c = 0;
        if (connectedNorth) c++; if (connectedSouth) c++;
        if (connectedEast)  c++; if (connectedWest)  c++;
        return c;
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

    /** Freccia verticale punta verso il BASSO in Pixmap (= verso SOUTH in world). */
    private static void drawArrowV(Pixmap p, int cx, int cy, int len,
                                   int color, int ox, int tileSize) {
        // Corpo verticale
        for (int ay = cy - len; ay <= cy + len / 3; ay++)
            for (int ax = cx - 1; ax <= cx + 1; ax++)
                if (ax >= ox && ax < ox + tileSize && ay >= 0 && ay < tileSize)
                    p.drawPixel(ax, ay, color);
        // Punta verso il basso
        for (int tip = 0; tip < 6; tip++) {
            int ty = cy + len / 3 + tip;
            for (int dx = -tip; dx <= tip; dx++) {
                int ax = cx + dx;
                if (ax >= ox && ax < ox + tileSize && ty >= 0 && ty < tileSize)
                    p.drawPixel(ax, ty, color);
            }
        }
    }

    private static void fillRect(Pixmap p, int x, int y, int w, int h, int color) {
        p.setColor(color); p.fillRectangle(x, y, w, h);
    }

    private static int rgba(float r, float g, float b, float a) {
        return (((int)(r*255)&0xFF)<<24)|(((int)(g*255)&0xFF)<<16)
              |(((int)(b*255)&0xFF)<< 8)|((int)(a*255)&0xFF);
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