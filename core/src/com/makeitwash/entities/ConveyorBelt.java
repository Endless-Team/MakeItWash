package com.makeitwash.entities;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.makeitwash.world.Grid;

/**
 * Nastro trasportatore con texture procedurali pixel art.
 *
 * ── SISTEMA DI COORDINATE ────────────────────────────────────────────────────
 * LibGDX SpriteBatch.draw() mappa la texture così (rotation=0):
 *   Pixmap (0,0)        → angolo TOP-LEFT  del quad in world
 *   Pixmap (TILE,0)     → angolo TOP-RIGHT del quad in world
 *   Pixmap (0,TILE)     → angolo BOT-LEFT  del quad in world
 *   Pixmap (TILE,TILE)  → angolo BOT-RIGHT del quad in world
 *
 * World Y-UP:
 *   Pixmap py=0    = top in Pixmap   = NORTH in world (Y alto)
 *   Pixmap py=TILE = bottom Pixmap   = SOUTH in world (Y basso)
 *   Pixmap px=0    = left Pixmap     = WEST  in world
 *   Pixmap px=TILE = right Pixmap    = EAST  in world
 *
 * ── ATLAS CURVO — geometria ───────────────────────────────────────────────────
 * Centro arco = angolo top-right (relX=TILE, py=0) = angolo NORTH-EAST in world.
 * Raggio = TILE = 64px. L'arco va da 90° a 180°:
 *   90°  → (TILE + cos90*TILE, sin90*TILE) = (TILE, TILE) = angolo BOT-RIGHT = SOUTH-EAST
 *   180° → (TILE + cos180*TILE, 0)         = (0, 0)       = angolo TOP-LEFT  = NORTH-WEST
 * La curva separa il tile da NORTH-WEST a SOUTH-EAST.
 *
 * INSIDE (dist < TILE dal centro NE): zona bottom-left = angolo SOUTH-WEST → ZONA ATTIVA
 * OUTSIDE (dist >= TILE):             zona top-right   = angolo NORTH-EAST → ZONA INATTIVA
 *
 * Dentro la zona attiva, la diagonale a 45° (relX + py = TILE) separa:
 *   relX + py < TILE  → vicino a NORTH-WEST: banda orizzontale py=[BELT_INNER..BELT_OUTER]
 *                        Lato di uscita: NORTH (top del tile, py=0)
 *                        → strisce orizzontali che scorrono ← verso sinistra (verso NORTH)
 *                          NO: le strisce mostrano il flusso. Il flusso esce da NORTH.
 *                          Banda in py=[BELT_INNER..BELT_OUTER] = fascia che tocca il lato WEST.
 *                          In world: lato WEST. Quindi questa è la zona di INGRESSO da WEST.
 *
 *   relX + py >= TILE → vicino a SOUTH-EAST: banda verticale relX=[BELT_INNER..BELT_OUTER]
 *                        Lato di uscita: NORTH (top del tile, py=0)
 *                        Banda verticale = fascia che tocca il lato NORTH (py=0).
 *                        → strisce verticali che scorrono ↑ verso NORTH = uscita NORTH.
 *
 * Quindi ATLAS BASE: ingresso WEST (sinistra) → uscita NORTH (sopra).
 * Le frecce:
 *   Zona ingresso WEST: frecce → verso destra (flusso entra da sinistra, scorre →)
 *   Zona uscita NORTH:  frecce ↑ verso l'alto (flusso sale verso NORTH)
 *
 * Rotazioni CCW SpriteBatch per tutte le curve:
 *   0°   → WEST→NORTH
 *   90°  → SOUTH→WEST
 *   180° → EAST→SOUTH
 *   270° → NORTH→EAST
 */
public class ConveyorBelt extends PlaceableEntity {

    // ── Animazione globale ────────────────────────────────────────────────────
    private static float globalProgress = 0f;
    private static float globalSpeed    = 1.2f;
    public static void tickGlobal(float delta) {
        globalProgress = (globalProgress + delta * globalSpeed) % 1f;
    }
    public static void setGlobalSpeed(float s) { globalSpeed = s; }
    public static float getGlobalProgress()    { return globalProgress; }

    // ── Direzioni ──────────────────────────────────────────────────────────────
    public enum Direction {
        NORTH, SOUTH, EAST, WEST;
        public Direction opposite() {
            switch (this) {
                case NORTH: return SOUTH; case SOUTH: return NORTH;
                case EAST:  return WEST;  case WEST:  return EAST;
                default: return EAST;
            }
        }
        public int dx() { return this==EAST?1:this==WEST?-1:0; }
        public int dy() { return this==NORTH?1:this==SOUTH?-1:0; }
    }

    // ── Stato flusso ──────────────────────────────────────────────────────────
    private Direction inputDirection  = Direction.WEST;
    private Direction outputDirection = Direction.EAST;
    private boolean connectedNorth, connectedSouth, connectedEast, connectedWest;

    // ── Costanti texture ──────────────────────────────────────────────────────
    public  static final int TILE      = 64;
    private static final int S_FRAMES  = 8;
    private static final int C_FRAMES  = 8;
    private static final int BELT_INNER = 10;
    private static final int BELT_OUTER = 54;

    // ── Palette ────────────────────────────────────────────────────────────────
    private static final int COL_BASE    = rgba(0.20f, 0.22f, 0.27f, 1f);
    private static final int COL_BELT    = rgba(0.27f, 0.30f, 0.35f, 1f);
    private static final int COL_INACTIVE= rgba(0.15f, 0.16f, 0.20f, 1f);
    private static final int COL_ZONE_IN = rgba(0.20f, 0.23f, 0.28f, 1f);
    private static final int COL_ZONE_OUT= rgba(0.23f, 0.26f, 0.31f, 1f);
    private static final int COL_EDGE    = rgba(0.12f, 0.13f, 0.16f, 1f);
    private static final int COL_ARC     = rgba(0.09f, 0.10f, 0.13f, 1f);
    private static final int COL_STRIPE  = rgba(0.44f, 0.48f, 0.55f, 1f);
    private static final int COL_ARROW   = rgba(0.70f, 0.76f, 0.85f, 1f);
    private static final int COL_SHADOW  = rgba(0.09f, 0.10f, 0.12f, 1f);

    // ── Texture statiche ──────────────────────────────────────────────────────
    private static Texture       straightTex, curveTex;
    private static TextureRegion straightReg, curveReg;
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
        final int beltTop = BELT_INNER, beltBot = BELT_OUTER;
        final int stripeStep = 10, stripeW = 3;

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
                for (int s = -TILE; s < TILE * 2; s += stripeStep)
                    for (int sw = 0; sw < stripeW; sw++) {
                        int drawX = ox + ((s + diagShift + sw) % TILE + TILE) % TILE;
                        if (drawX >= ox && drawX < ox + TILE)
                            p.drawPixel(drawX, row, COL_STRIPE);
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
     * ATLAS BASE: WEST ingresso (sinistra) → NORTH uscita (sopra).
     *
     * Geometria Pixmap (Y-down, px=0 a sinistra):
     *
     *   Centro arco C = (TILE, 0) = angolo top-right del Pixmap.
     *   Per ogni pixel (relX, py):
     *     dist = sqrt((TILE-relX)^2 + py^2)
     *     inside = dist < TILE
     *
     *   Zona INATTIVA (outside): sfondo scuro.
     *   Zona ATTIVA (inside): divisa dalla diagonale relX + py = TILE:
     *
     *     Sub-zona INGRESSO (relX + py >= TILE, cioè vicino al corner SOUTH-EAST Pixmap):
     *       = WEST side in world (lato sinistro)
     *       Fascia verticale: relX in [BELT_INNER, BELT_OUTER]
     *       Strisce ORIZZONTALI che scorrono → verso destra (= verso l'interno del tile)
     *       Freccia → verso destra
     *
     *     Sub-zona USCITA (relX + py < TILE, cioè vicino al corner NORTH-WEST Pixmap):
     *       = NORTH side in world (lato superiore)
     *       Fascia orizzontale: py in [BELT_INNER, BELT_OUTER]
     *       Strisce VERTICALI che scorrono ↑ verso il top (= verso NORTH, uscita)
     *       Freccia ↑ verso l'alto
     */
    private static Texture buildCurveAtlas() {
        Pixmap p = new Pixmap(TILE * C_FRAMES, TILE, Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);

        for (int f = 0; f < C_FRAMES; f++) {
            int ox = f * TILE;

            // Sfondo base
            fillRect(p, ox, 0, TILE, TILE, COL_BASE);

            // Offset animazione
            int animOffset = (f * TILE) / C_FRAMES;

            for (int py = 0; py < TILE; py++) {
                for (int relX = 0; relX < TILE; relX++) {
                    int px = ox + relX;

                    // Distanza dal centro arco (top-right del tile: relX=TILE, py=0)
                    double dx   = TILE - relX;
                    double dy   = (double) py;
                    double dist = Math.sqrt(dx * dx + dy * dy);

                    if (dist >= TILE) {
                        // ZONA INATTIVA
                        p.drawPixel(px, py, COL_INACTIVE);
                        continue;
                    }

                    // ZONA ATTIVA: dentro l'arco
                    // Sub-zona ingresso WEST: relX + py >= TILE (vicino a bottom-right Pixmap)
                    boolean isIngresso = (relX + py) >= TILE;

                    if (isIngresso) {
                        // Fascia verticale [BELT_INNER..BELT_OUTER] sul lato WEST
                        boolean inBand = relX >= BELT_INNER && relX <= BELT_OUTER;
                        p.drawPixel(px, py, inBand ? COL_BELT : COL_ZONE_IN);
                        if (inBand) {
                            // Strisce orizzontali animate → verso destra
                            int strX = ((relX - animOffset) % 10 + 10) % 10;
                            if (strX < 3) p.drawPixel(px, py, COL_STRIPE);
                            // Bordi fascia
                            if (relX == BELT_INNER || relX == BELT_INNER+1 || relX == BELT_INNER+2)
                                p.drawPixel(px, py, COL_EDGE);
                            if (relX == BELT_OUTER-2 || relX == BELT_OUTER-1 || relX == BELT_OUTER)
                                p.drawPixel(px, py, COL_EDGE);
                        }
                    } else {
                        // Sub-zona uscita NORTH: relX + py < TILE (vicino a top-left Pixmap)
                        // Fascia orizzontale [BELT_INNER..BELT_OUTER] sul lato NORTH
                        boolean inBand = py >= BELT_INNER && py <= BELT_OUTER;
                        p.drawPixel(px, py, inBand ? COL_BELT : COL_ZONE_OUT);
                        if (inBand) {
                            // Strisce verticali animate ↑ verso NORTH (py decrescente = animOffset negativo)
                            int strY = ((py + animOffset) % 10 + 10) % 10;
                            if (strY < 3) p.drawPixel(px, py, COL_STRIPE);
                            // Bordi fascia
                            if (py == BELT_INNER || py == BELT_INNER+1 || py == BELT_INNER+2)
                                p.drawPixel(px, py, COL_EDGE);
                            if (py == BELT_OUTER-2 || py == BELT_OUTER-1 || py == BELT_OUTER)
                                p.drawPixel(px, py, COL_EDGE);
                        }
                    }
                }
            }

            // ── Arco di separazione (3px) ─────────────────────────────────────
            // Centro (TILE, 0), angoli 90°→180°
            // cos(90°)=0, sin(90°)=1 → punto (TILE, TILE) = bottom-right  ✓
            // cos(180°)=-1, sin(180°)=0 → punto (0, 0) = top-left         ✓
            for (double angle = 90.0; angle <= 180.0; angle += 0.3) {
                double rad = Math.toRadians(angle);
                double arcX = TILE + Math.cos(rad) * TILE;
                double arcY =        Math.sin(rad) * TILE;
                for (int t = -1; t <= 1; t++) {
                    int ppx = ox + (int) Math.round(arcX) + t;
                    int ppy =      (int) Math.round(arcY);
                    if (ppx >= ox && ppx < ox + TILE && ppy >= 0 && ppy < TILE)
                        p.drawPixel(ppx, ppy, COL_ARC);
                    ppx = ox + (int) Math.round(arcX);
                    ppy =      (int) Math.round(arcY) + t;
                    if (ppx >= ox && ppx < ox + TILE && ppy >= 0 && ppy < TILE)
                        p.drawPixel(ppx, ppy, COL_ARC);
                }
            }

            // ── Diagonale 45° interna (separa le due sub-zone) ────────────────
            // relX + py = TILE → per ogni py: relX = TILE - py
            for (int py = 0; py < TILE; py++) {
                int relX = TILE - py;
                double dxx = TILE - relX, dyy = (double) py;
                if (Math.sqrt(dxx*dxx + dyy*dyy) < TILE) { // solo dentro l'arco
                    for (int t = -1; t <= 1; t++) {
                        int ppx = ox + relX + t;
                        if (ppx >= ox && ppx < ox + TILE)
                            p.drawPixel(ppx, py, COL_ARC);
                    }
                }
            }

            // ── Frecce ────────────────────────────────────────────────────────
            // Freccia ingresso WEST: orizzontale → destra
            // Centro nella banda verticale, metà della zona ingresso
            int arrowInY = TILE * 3 / 4; // py vicino a bottom = zona ingresso
            int arrowInX = ox + (BELT_INNER + BELT_OUTER) / 2; // centro della fascia
            if ((BELT_INNER + BELT_OUTER)/2 + arrowInY >= TILE) { // è dentro la zona ingresso?
                drawArrowH(p, arrowInX, arrowInY, 8, COL_ARROW, 0, TILE);
            }

            // Freccia uscita NORTH: verticale ↑ verso il top
            // Centro nella banda orizzontale, metà della zona uscita
            int arrowOutX = ox + TILE / 4; // relX vicino a 0 = zona uscita
            int arrowOutY = (BELT_INNER + BELT_OUTER) / 2; // centro della fascia
            if (arrowOutX - ox + arrowOutY < TILE) { // è dentro la zona uscita?
                drawArrowUp(p, arrowOutX, arrowOutY, 8, COL_ARROW, ox, TILE);
            }

            // ── Ombre bordi ───────────────────────────────────────────────────
            for (int i = 0; i < TILE; i++) {
                p.drawPixel(ox + i, 0,        COL_SHADOW);
                p.drawPixel(ox + i, TILE - 1, COL_SHADOW);
                p.drawPixel(ox + 0, i,        COL_SHADOW);
                p.drawPixel(ox + i, 1,        COL_SHADOW);
                p.drawPixel(ox + i, TILE - 2, COL_SHADOW);
                p.drawPixel(ox + 1, i,        COL_SHADOW);
            }

            // ── Bordi continuità con nastri adiacenti ─────────────────────────
            // Lato WEST (ingresso): bordi della fascia verticale
            for (int bw = 0; bw < 3; bw++) {
                // Bordo INNER fascia verticale (px = BELT_INNER+bw, tutta l'altezza)
                for (int py = 0; py < TILE; py++)
                    p.drawPixel(ox + BELT_INNER + bw, py, COL_EDGE);
                // Bordo OUTER fascia verticale
                for (int py = 0; py < TILE; py++)
                    p.drawPixel(ox + BELT_OUTER - bw, py, COL_EDGE);
            }
            // Lato NORTH (uscita): bordi della fascia orizzontale
            for (int bw = 0; bw < 3; bw++) {
                for (int relX = 0; relX < TILE; relX++) {
                    p.drawPixel(ox + relX, BELT_INNER + bw, COL_EDGE);
                    p.drawPixel(ox + relX, BELT_OUTER - bw, COL_EDGE);
                }
            }
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

    // ── Connessioni ────────────────────────────────────────────────────────────
    public void updateConnections(Grid grid) {
        connectedNorth = grid.hasConveyorAt(gridX, gridY + 1);
        connectedSouth = grid.hasConveyorAt(gridX, gridY - 1);
        connectedEast  = grid.hasConveyorAt(gridX + 1, gridY);
        connectedWest  = grid.hasConveyorAt(gridX - 1, gridY);
    }

    // ── Flusso ─────────────────────────────────────────────────────────────────
    public void applyFlow(Direction from) {
        this.inputDirection = from;
        if (isCurveConnection()) {
            // Esclude il lato di INGRESSO (= from) dalla ricerca del lato di uscita.
            // VECCHIO ERRORE: phys = from.opposite() escludeva il lato OPPOSTO all'ingresso
            // invece del lato d'ingresso stesso. Per alcune curve questo causava
            // outputDirection = lato d'ingresso (loop su se stesso).
            // Esempio: curve connectedSouth+connectedWest, applyFlow(SOUTH):
            //   OLD phys=NORTH  → trova SOUTH (l'entry!) → outputDir=SOUTH ✗
            //   NEW phys=SOUTH  → salta SOUTH, trova WEST → outputDir=WEST ✓
            Direction phys = from;  // ← era from.opposite(), SBAGLIATO
            for (Direction d : Direction.values())
                if (isConnectedIn(d) && d != phys) { outputDirection = d; return; }
        } else {
            outputDirection = from.opposite();
        }
    }

    private boolean isConnectedIn(Direction d) {
        switch (d) {
            case NORTH: return connectedNorth; case SOUTH: return connectedSouth;
            case EAST:  return connectedEast;  case WEST:  return connectedWest;
            default: return false;
        }
    }

    public int nextGridX() { return gridX + outputDirection.dx(); }
    public int nextGridY() { return gridY + outputDirection.dy(); }

    // ── Update / Render ────────────────────────────────────────────────────────
    @Override public void update(float delta) {}

    @Override
    public void render(SpriteBatch batch) {
        if (straightTex == null) return;
        boolean curve = isCurveConnection();
        int frames    = curve ? C_FRAMES : S_FRAMES;
        int frame     = (int)(globalProgress * frames) % frames;
        float rot     = getRotation();
        if (curve) {
            curveReg.setRegionX(frame * TILE); curveReg.setRegionWidth(TILE);
            batch.draw(curveReg, x, y, TILE/2f, TILE/2f, TILE, TILE, 1f, 1f, rot);
        } else {
            straightReg.setRegionX(frame * TILE); straightReg.setRegionWidth(TILE);
            batch.draw(straightReg, x, y, TILE/2f, TILE/2f, TILE, TILE, 1f, 1f, rot);
        }
    }

    // =========================================================================
    // Rotazione visiva
    // =========================================================================
    /**
     * NASTRI DRITTI:
     *   0°→EAST, 90°→NORTH, 180°→WEST, 270°→SOUTH
     *
     * NASTRI CURVI — atlas base: WEST ingresso → NORTH uscita
     * Rotazioni CCW SpriteBatch:
     *   0°   → WEST→NORTH
     *   90°  → SOUTH→WEST
     *   180° → EAST→SOUTH
     *   270° → NORTH→EAST
     *
     * Verifica con rotazione 90° CCW:
     *   Il lato WEST dell'atlas base (sinistra) va in BASSO → SOUTH   ✓ ingresso SOUTH
     *   Il lato NORTH dell'atlas base (sopra)   va a SINISTRA → WEST  ✓ uscita WEST
     */
    private float getRotation() {
        if (isCurveConnection()) {
            // ── CONVEZIONE inputDirection ──────────────────────────────────────
            // applyFlow(from): "from" = lato fisico da cui ENTRA il flusso.
            //   applyFlow(WEST) → inputDirection=WEST → il flusso arriva dal lato WEST.
            // Per la rotazione vogliamo il lato fisico di ingresso = inputDirection (NO .opposite()).
            //
            // ── TABELLA ROTAZIONI (CCW SpriteBatch, zona attiva = SW dell'atlas) ──
            //   0°   → zona SW world → entry WEST,  exit NORTH  ← atlas base
            //   90°  → zona SE world → entry SOUTH, exit WEST
            //   180° → zona NE world → entry EAST,  exit SOUTH
            //   270° → zona NW world → entry NORTH, exit EAST
            //
            // Se il tuo Grid chiama applyFlow con la direzione OPPOSTA (cioè
            // applyFlow(EAST) per "viene da ovest"), basta invertire in/out qui sotto.
            Direction in  = inputDirection;  // ← NIENTE .opposite()
            Direction out = outputDirection;
            // Ogni forma fisica ha 2 direzioni di flusso possibili (forward/reverse).
            // Tutte e 8 le combinazioni devono essere coperte.
            if ((in == Direction.WEST  && out == Direction.NORTH) ||
                (in == Direction.NORTH && out == Direction.WEST))  return   0f;
            if ((in == Direction.SOUTH && out == Direction.WEST)  ||
                (in == Direction.WEST  && out == Direction.SOUTH)) return  90f;
            if ((in == Direction.EAST  && out == Direction.SOUTH) ||
                (in == Direction.SOUTH && out == Direction.EAST))  return 180f;
            if ((in == Direction.NORTH && out == Direction.EAST)  ||
                (in == Direction.EAST  && out == Direction.NORTH)) return 270f;
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

    // ── Helpers ─────────────────────────────────────────────────────────────────
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
        for (int ax = cx - len; ax <= cx + len/3; ax++)
            for (int ay = cy - 1; ay <= cy + 1; ay++)
                if (ay >= clipTop && ay < clipBot) p.drawPixel(ax, ay, color);
        for (int tip = 0; tip < 7; tip++) {
            int tx = cx + len/3 + tip;
            for (int dy = -tip; dy <= tip; dy++) {
                int ay = cy + dy;
                if (ay >= clipTop && ay < clipBot) p.drawPixel(tx, ay, color);
            }
        }
    }

    /** Freccia verticale che punta verso l'ALTO in Pixmap (= verso NORTH in world). */
    private static void drawArrowUp(Pixmap p, int cx, int cy, int len,
                                    int color, int ox, int tileSize) {
        // Corpo verticale (verso il basso dal centro, cioè in Pixmap py crescente)
        for (int ay = cy; ay <= cy + len; ay++)
            for (int ax = cx - 1; ax <= cx + 1; ax++)
                if (ax >= ox && ax < ox + tileSize && ay >= 0 && ay < tileSize)
                    p.drawPixel(ax, ay, color);
        // Punta verso l'alto (py decrescente = NORTH in world)
        for (int tip = 0; tip < 6; tip++) {
            int ty = cy - tip;
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
              |(((int)(b*255)&0xFF)<<8)|((int)(a*255)&0xFF);
    }

    // ── Getters ──────────────────────────────────────────────────────────────────
    public Direction getInputDirection()  { return inputDirection; }
    public Direction getOutputDirection() { return outputDirection; }
    public boolean isConnectedNorth()     { return connectedNorth; }
    public boolean isConnectedSouth()     { return connectedSouth; }
    public boolean isConnectedEast()      { return connectedEast; }
    public boolean isConnectedWest()      { return connectedWest; }
}