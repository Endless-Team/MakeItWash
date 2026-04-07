package com.makeitwash.entities;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.makeitwash.world.Grid;

public class ConveyorBelt extends PlaceableEntity {

    // =========================================================================
    // ANIMAZIONE GLOBALE SINCRONIZZATA
    // =========================================================================
    private static float globalProgress = 0f;
    private static float globalSpeed    = 1.2f; // cicli/sec

    public static void tickGlobal(float delta) {
        globalProgress += delta * globalSpeed;
        if (globalProgress >= 1f) globalProgress -= 1f;
    }
    public static void setGlobalSpeed(float s) { globalSpeed = s; }
    public static float getGlobalProgress()    { return globalProgress; }

    // =========================================================================
    // Stato connessioni e orientamento
    // =========================================================================
    private boolean   curved           = false;
    private boolean   connectedNorth   = false;
    private boolean   connectedSouth   = false;
    private boolean   connectedEast    = false;
    private boolean   connectedWest    = false;
    private Direction outputDirection  = Direction.EAST;

    public enum Direction { NORTH, SOUTH, EAST, WEST }

    // =========================================================================
    // Texture / atlas
    // =========================================================================
    private static Texture       straightTex;
    private static Texture       curveTex;
    private static TextureRegion straightRegion;
    private static TextureRegion curveRegion;
    private static boolean       loaded = false;

    static final int TILE      = 64;
    private static final int S_FRAMES = 8;  // frame atlas dritto
    private static final int C_FRAMES = 8;  // frame atlas curvo

    // --- Palette colori -------------------------------------------------------
    // sfondo tile
    private static final int COL_BASE   = rgba(0.22f, 0.24f, 0.28f, 1f);
    // fascia del nastro (leggermente più chiara del base per essere visibile)
    private static final int COL_BELT   = rgba(0.28f, 0.31f, 0.36f, 1f);
    // bordi nastro (scuro)
    private static final int COL_EDGE   = rgba(0.13f, 0.14f, 0.17f, 1f);
    // strisce chiare animate
    private static final int COL_STRIPE = rgba(0.46f, 0.50f, 0.56f, 1f);
    // freccia direzione
    private static final int COL_ARROW  = rgba(0.72f, 0.78f, 0.86f, 1f);
    // ombra bordo tile
    private static final int COL_SHADOW = rgba(0.10f, 0.11f, 0.13f, 1f);

    // =========================================================================
    // Init / Dispose
    // =========================================================================
    public static void ensureTexturesLoaded() {
        if (loaded) return;
        loaded      = true;
        straightTex = buildStraightAtlas();
        curveTex    = buildCurveAtlas();
        straightRegion = new TextureRegion(straightTex, 0, 0, TILE, TILE);
        curveRegion    = new TextureRegion(curveTex,    0, 0, TILE, TILE);
    }

    public static void disposeTextures() {
        if (straightTex != null) { straightTex.dispose(); straightTex = null; }
        if (curveTex    != null) { curveTex.dispose();    curveTex    = null; }
        straightRegion = null;
        curveRegion    = null;
        loaded = false;
    }

    // =========================================================================
    // ATLAS NASTRO DRITTO
    // =========================================================================
    /**
     * S_FRAMES tile affiancati orizzontalmente.
     * Ogni frame ha le strisce diagonali spostate di un passo.
     * Atlas base orientato EAST (→); la rotazione in render() copre
     * NORTH / WEST / SOUTH.
     *
     *  beltTop  ┌─────────────────────────────────┐
     *           │ \\  \\  \\  \\  \\  \\  \\  \\  →→→     │
     *  beltBot  └─────────────────────────────────┘
     */
    private static Texture buildStraightAtlas() {
        int W = TILE * S_FRAMES;
        Pixmap p = new Pixmap(W, TILE, Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);

        int stripeSpacing = 10;
        int stripeW       = 3;
        int beltTop       = 8;
        int beltBot       = TILE - 8;

        for (int f = 0; f < S_FRAMES; f++) {
            int ox = f * TILE;

            // 1. Sfondo
            fillRect(p, ox, 0, TILE, TILE, COL_BASE);

            // 2. Ombra bordi tile
            fillRect(p, ox, 0,         TILE, 2, COL_SHADOW);
            fillRect(p, ox, TILE - 2,  TILE, 2, COL_SHADOW);

            // 3. Fascia nastro
            fillRect(p, ox, beltTop, TILE, beltBot - beltTop, COL_BELT);

            // 4. Bordi nastro
            fillRect(p, ox, beltTop,     TILE, 3, COL_EDGE);
            fillRect(p, ox, beltBot - 3, TILE, 3, COL_EDGE);

            // 5. Strisce diagonali animate
            int offset = (f * TILE) / S_FRAMES;
            for (int sx = -TILE; sx < TILE * 2; sx += stripeSpacing) {
                for (int row = beltTop + 3; row < beltBot - 3; row++) {
                    int diag = offset + (row - beltTop) / 2;
                    for (int sw = 0; sw < stripeW; sw++) {
                        int drawX = ox + ((sx + diag + sw) % TILE + TILE) % TILE;
                        if (drawX >= ox && drawX < ox + TILE)
                            p.drawPixel(drawX, row, COL_STRIPE);
                    }
                }
            }

            // 6. Freccia → al centro
            int acy = TILE / 2;
            int acx = ox + TILE / 2;
            // corpo
            for (int ax = acx - 10; ax <= acx + 4; ax++)
                for (int ay = acy - 1; ay <= acy + 1; ay++)
                    if (ay >= beltTop + 4 && ay < beltBot - 4)
                        p.drawPixel(ax, ay, COL_ARROW);
            // punta
            for (int tip = 0; tip < 6; tip++) {
                int tipX  = acx + 4 + tip;
                int tipY1 = acy - tip;
                int tipY2 = acy + tip;
                if (tipY1 >= beltTop + 4 && tipY2 < beltBot - 4) {
                    p.drawPixel(tipX, tipY1, COL_ARROW);
                    p.drawPixel(tipX, tipY2, COL_ARROW);
                }
            }
        }

        Texture t = new Texture(p);
        // Nearest = pixel art netta, nessun blur
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        p.dispose();
        return t;
    }

    // =========================================================================
    // ATLAS NASTRO CURVO
    // =========================================================================
    /**
     * C_FRAMES tile affiancati. Ogni frame è una snapshot dell'arco con
     * le strisce spostate di un passo, creando l'animazione di scorrimento.
     *
     * GEOMETRIA (coordinate Pixmap: Y=0 in alto, Y=TILE in basso):
     *   Centro arco → angolo BOTTOM-RIGHT del tile: (TILE, TILE)
     *   Arco da 180° a 270° (quadrante top-left rispetto al centro)
     *     180° → (TILE - r,  TILE    ) = bordo WEST  (ingresso)
     *     270° → (TILE,      TILE - r) = bordo NORTH (uscita)
     *
     * Con LibGDX SpriteBatch.draw() che ruota attorno al centro del tile,
     * le 4 varianti si ottengono con:
     *   0°   → WEST→NORTH  (atlas base)
     *   90°  → SOUTH→WEST
     *   180° → EAST→SOUTH
     *   270° → NORTH→EAST
     */
    private static Texture buildCurveAtlas() {
        int W = TILE * C_FRAMES;
        Pixmap p = new Pixmap(W, TILE, Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);

        // Centro arco = angolo bottom-right del tile
        final float arcCX = TILE;
        final float arcCY = TILE;

        // Raggi della fascia nastro
        final int beltInner = 16;
        final int beltOuter = 48;
        final int beltMid   = (beltInner + beltOuter) / 2;

        // Arco da 180° a 270° (Math standard, Y verso il basso in Pixmap)
        final double startDeg = 180.0;
        final double endDeg   = 270.0;
        final double totalArc = endDeg - startDeg; // 90°

        final int stripeCount = 6;

        for (int f = 0; f < C_FRAMES; f++) {
            int ox = f * TILE;

            // 1. Sfondo
            fillRect(p, ox, 0, TILE, TILE, COL_BASE);

            // 2. Ombra bordi tile
            fillRect(p, ox, 0,    TILE, 2, COL_SHADOW);
            fillRect(p, ox, 0,    2, TILE, COL_SHADOW);

            // 3. Fascia piena dell'arco — riempimento con COL_BELT
            for (double deg = startDeg; deg <= endDeg; deg += 0.3) {
                double rad  = Math.toRadians(deg);
                double cosA = Math.cos(rad);
                double sinA = Math.sin(rad);
                for (int r = beltInner; r <= beltOuter; r++) {
                    int px = ox + (int)(arcCX + r * cosA);
                    int py =      (int)(arcCY + r * sinA);
                    if (px >= ox && px < ox + TILE && py >= 0 && py < TILE)
                        p.drawPixel(px, py, COL_BELT);
                }
            }

            // 4. Bordi nastro inner / outer
            drawArcLine(p, ox, arcCX, arcCY, beltInner, 3, startDeg, endDeg, COL_EDGE);
            drawArcLine(p, ox, arcCX, arcCY, beltOuter, 3, startDeg, endDeg, COL_EDGE);

            // 5. Strisce animate lungo la fascia
            double degPerStripe  = totalArc / stripeCount;
            double frameOffsetDeg = ((double) f / C_FRAMES) * degPerStripe;

            for (int s = 0; s < stripeCount + 1; s++) {
                double sd = startDeg + (s * degPerStripe - frameOffsetDeg);
                double ed = sd + 3.0;
                if (ed < startDeg || sd > endDeg) continue;
                sd = Math.max(sd, startDeg);
                ed = Math.min(ed, endDeg);
                drawArcLine(p, ox, arcCX, arcCY, beltMid, 5, sd, ed, COL_STRIPE);
            }

            // 6. Freccia a metà arco (225°, tangente al moto)
            double arrowDeg = 225.0;
            double arrowRad = Math.toRadians(arrowDeg);
            int arrowX = ox + (int)(arcCX + beltMid * Math.cos(arrowRad));
            int arrowY =      (int)(arcCY + beltMid * Math.sin(arrowRad));

            // Tangente: perpendicolare al raggio, nel verso del moto (CCW)
            double tanRad = arrowRad - Math.PI / 2.0;
            double tx = Math.cos(tanRad);
            double ty = Math.sin(tanRad);

            for (int i = -5; i <= 5; i++) {
                int fx2 = arrowX + (int)(tx * i);
                int fy2 = arrowY + (int)(ty * i);
                if (fx2 >= ox && fx2 < ox + TILE && fy2 >= 0 && fy2 < TILE)
                    p.drawPixel(fx2, fy2, COL_ARROW);
            }
            // Punta freccia
            for (int tip = 1; tip <= 4; tip++) {
                int fx2 = arrowX + (int)(tx * 5) + (int)(-ty * tip);
                int fy2 = arrowY + (int)(ty * 5) + (int)( tx * tip);
                if (fx2 >= ox && fx2 < ox + TILE && fy2 >= 0 && fy2 < TILE)
                    p.drawPixel(fx2, fy2, COL_ARROW);
                fx2 = arrowX + (int)(tx * 5) + (int)(ty * tip);
                fy2 = arrowY + (int)(ty * 5) + (int)(-tx * tip);
                if (fx2 >= ox && fx2 < ox + TILE && fy2 >= 0 && fy2 < TILE)
                    p.drawPixel(fx2, fy2, COL_ARROW);
            }

            // 7. Bordi di continuità per allineamento con nastri adiacenti
            // Ingresso WEST (lato sinistro del tile): righe beltInner..beltOuter
            fillRect(p, ox, beltInner, 2, beltOuter - beltInner, COL_EDGE);
            // Uscita NORTH (lato superiore del tile): colonne beltInner..beltOuter
            fillRect(p, ox + beltInner, 0, beltOuter - beltInner, 2, COL_EDGE);
        }

        Texture t = new Texture(p);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        p.dispose();
        return t;
    }

    // =========================================================================
    // Helpers pixel drawing
    // =========================================================================
    private static void fillRect(Pixmap p, int x, int y, int w, int h, int color) {
        p.setColor(color);
        p.fillRectangle(x, y, w, h);
    }

    /**
     * Disegna una linea ad arco (settore anulare) centrata in (cx,cy),
     * al raggio 'radius', con spessore 'thickness', da startDeg a endDeg.
     */
    private static void drawArcLine(Pixmap p, int ox,
                                    double cx, double cy,
                                    int radius, int thickness,
                                    double startDeg, double endDeg,
                                    int color) {
        p.setColor(color);
        int half = thickness / 2;
        for (double deg = startDeg; deg <= endDeg; deg += 0.4) {
            double rad  = Math.toRadians(deg);
            double cosA = Math.cos(rad);
            double sinA = Math.sin(rad);
            for (int t = -half; t <= half; t++) {
                int px = ox + (int)(cx + (radius + t) * cosA);
                int py =      (int)(cy + (radius + t) * sinA);
                if (px >= ox && px < ox + TILE && py >= 0 && py < TILE)
                    p.drawPixel(px, py);
            }
        }
    }

    private static int rgba(float r, float g, float b, float a) {
        int ri = (int)(r * 255) & 0xFF;
        int gi = (int)(g * 255) & 0xFF;
        int bi = (int)(b * 255) & 0xFF;
        int ai = (int)(a * 255) & 0xFF;
        return (ri << 24) | (gi << 16) | (bi << 8) | ai;
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
    // Connessioni e orientamento
    // =========================================================================
    public void updateConnections(Grid grid) {
        updateConnections(grid, null);
    }

    public void updateConnections(Grid grid, Direction fromDirection) {
        connectedNorth = grid.hasConveyorAt(gridX, gridY + 1);
        connectedSouth = grid.hasConveyorAt(gridX, gridY - 1);
        connectedEast  = grid.hasConveyorAt(gridX + 1, gridY);
        connectedWest  = grid.hasConveyorAt(gridX - 1, gridY);
        this.curved          = isCurveConnection();
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
    public void update(float delta) { /* tick globale in Grid.update() */ }

    @Override
    public void render(SpriteBatch batch) {
        if (straightTex == null) return;

        float rotation = getRotation();
        float cx = TILE / 2f;
        float cy = TILE / 2f;

        boolean isCurve = isCurveConnection();
        int totalFrames = isCurve ? C_FRAMES : S_FRAMES;
        int frame = (int)(globalProgress * totalFrames) % totalFrames;

        if (isCurve) {
            curveRegion.setRegionX(frame * TILE);
            curveRegion.setRegionWidth(TILE);
            batch.draw(curveRegion, x, y, cx, cy, TILE, TILE, 1f, 1f, rotation);
        } else {
            straightRegion.setRegionX(frame * TILE);
            straightRegion.setRegionWidth(TILE);
            batch.draw(straightRegion, x, y, cx, cy, TILE, TILE, 1f, 1f, rotation);
        }
    }

    // =========================================================================
    // Rotazione visiva
    // =========================================================================
    /**
     * NASTRI DRITTI — rotazione basata su outputDirection:
     *   0°   → EAST  (default atlas, strisce → destra)
     *   90°  → NORTH (strisce verso l'alto)
     *   180° → WEST
     *   270° → SOUTH
     *
     * NASTRI CURVI — atlas base: ingresso WEST (←), uscita NORTH (↑)
     *   Centro arco = angolo bottom-right; arco 180°→270°
     *
     *   Rotazione 0°   (default)   → WEST  ingresso, NORTH uscita
     *   Rotazione 90°  (CCW)       → SOUTH ingresso, WEST  uscita
     *   Rotazione 180°             → EAST  ingresso, SOUTH uscita
     *   Rotazione 270° (CW)        → NORTH ingresso, EAST  uscita
     *
     * Tabella combinazioni connessioni → rotazione:
     *   WEST  + NORTH  →   0°
     *   SOUTH + WEST   →  90°
     *   EAST  + SOUTH  → 180°
     *   NORTH + EAST   → 270°
     */
    private float getRotation() {
        if (isCurveConnection()) {
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
    // Helpers
    // =========================================================================
    private boolean isCurveConnection() {
        int conn = countConnections();
        if (conn != 2) return false;
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

    // Getters
    public boolean   isCurved()            { return curved; }
    public Direction getOutputDirection()  { return outputDirection; }
    public boolean   isConnectedNorth()    { return connectedNorth; }
    public boolean   isConnectedSouth()    { return connectedSouth; }
    public boolean   isConnectedEast()     { return connectedEast; }
    public boolean   isConnectedWest()     { return connectedWest; }
}