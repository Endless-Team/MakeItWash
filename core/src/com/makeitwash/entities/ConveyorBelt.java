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
    private static float globalSpeed = 1.2f; // cicli/sec

    public static void tickGlobal(float delta) {
        globalProgress += delta * globalSpeed;
        if (globalProgress >= 1f)
            globalProgress -= 1f;
    }

    public static void setGlobalSpeed(float s) {
        globalSpeed = s;
    }

    public static float getGlobalProgress() {
        return globalProgress;
    }

    // =========================================================================
    // Stato connessioni e orientamento
    // =========================================================================
    private boolean curved = false;

    private boolean connectedNorth = false;
    private boolean connectedSouth = false;
    private boolean connectedEast = false;
    private boolean connectedWest = false;

    private Direction outputDirection = Direction.EAST;

    public enum Direction {
        NORTH, SOUTH, EAST, WEST
    }

    // =========================================================================
    // Texture / atlas
    // =========================================================================
    private static Texture straightTex;
    private static Texture curveTex;
    private static TextureRegion straightRegion;
    private static TextureRegion curveRegion;
    private static boolean loaded = false;

    static final int TILE = 64;
    private static final int S_FRAMES = 8; // frame nell'atlas dritto
    private static final int C_FRAMES = 8; // frame nell'atlas curvo

    // Palette
    private static final int COL_BASE = rgba(0.22f, 0.24f, 0.28f, 1f); // sfondo scuro
    private static final int COL_EDGE = rgba(0.14f, 0.15f, 0.18f, 1f); // bordi nastro
    private static final int COL_STRIPE = rgba(0.46f, 0.50f, 0.56f, 1f); // strisce chiare
    private static final int COL_ARROW = rgba(0.72f, 0.78f, 0.86f, 1f); // freccia direzione
    private static final int COL_SHADOW = rgba(0.10f, 0.11f, 0.13f, 1f); // ombra bordo tile

    // =========================================================================
    // Init / Dispose
    // =========================================================================
    public static void ensureTexturesLoaded() {
        if (loaded)
            return;
        loaded = true;
        straightTex = buildStraightAtlas();
        curveTex = buildCurveAtlas();
        straightRegion = new TextureRegion(straightTex, 0, 0, TILE, TILE);
        curveRegion = new TextureRegion(curveTex, 0, 0, TILE, TILE);
    }

    public static void disposeTextures() {
        if (straightTex != null) {
            straightTex.dispose();
            straightTex = null;
        }
        if (curveTex != null) {
            curveTex.dispose();
            curveTex = null;
        }
        straightRegion = null;
        curveRegion = null;
        loaded = false;
    }

    // =========================================================================
    // ATLAS NASTRO DRITTO
    // =========================================================================
    /**
     * S_FRAMES tile affiancati. Ogni frame ha le strisce diagonali spostate di
     * un passo, più una freccia fissa al centro.
     *
     * Layout per frame:
     * - Bordi laterali (ombre) sulle righe 0-3 e (TILE-4)-(TILE-1)
     * - Strisce diagonali sottili che scorron da destra a sinistra
     * - Freccia triangolare al centro che indica la direzione EAST (→)
     * (la rotazione in render() la orienta correttamente)
     */
    private static Texture buildStraightAtlas() {
        int W = TILE * S_FRAMES;
        Pixmap p = new Pixmap(W, TILE, Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);

        int stripeSpacing = 10; // pixel tra strisce
        int stripeW = 3; // larghezza striscia
        int beltTop = 6; // inizio nastro (bordo superiore)
        int beltBot = TILE - 6; // fine nastro (bordo inferiore)

        for (int f = 0; f < S_FRAMES; f++) {
            int ox = f * TILE;

            // 1. Sfondo totale del tile
            fillRect(p, ox, 0, TILE, TILE, COL_BASE);

            // 2. Ombra bordi tile (per distinguere tile adiacenti)
            fillRect(p, ox, 0, TILE, 2, COL_SHADOW);
            fillRect(p, ox, TILE - 2, TILE, 2, COL_SHADOW);

            // 3. Bordi del nastro (righe di separazione)
            fillRect(p, ox, beltTop - 3, TILE, 3, COL_EDGE);
            fillRect(p, ox, beltBot, TILE, 3, COL_EDGE);

            // 4. Strisce diagonali animate (si spostano di 1px per frame)
            // Le strisce sono inclinate a ~30° verso sinistra per dare
            // l'impressione di moto verso destra.
            int offset = (f * TILE) / S_FRAMES;
            for (int sx = -TILE; sx < TILE * 2; sx += stripeSpacing) {
                for (int row = beltTop; row < beltBot; row++) {
                    // inclinazione: ogni riga sposta la striscia di 0.5px
                    int col = ox + ((sx + offset + (row - beltTop) / 2) % TILE + TILE) % TILE;
                    for (int sw = 0; sw < stripeW; sw++) {
                        int drawX = ox + ((sx + offset + (row - beltTop) / 2) % TILE + TILE) % TILE;
                        // int drawX = (col + sw - ox + TILE) % TILE + ox;
                        p.drawPixel(drawX, row, COL_STRIPE);
                    }
                }
            }

            // 5. Freccia → al centro (indica direzione EAST, rotata in render)
            int acy = TILE / 2; // centro Y
            int acx = ox + TILE / 2; // centro X del frame
            // Corpo della freccia (linea orizzontale)
            for (int ax = acx - 10; ax <= acx + 4; ax++) {
                for (int ay = acy - 1; ay <= acy + 1; ay++) {
                    if (ay >= beltTop && ay < beltBot)
                        p.drawPixel(ax, ay, COL_ARROW);
                }
            }
            // Punta triangolare destra
            for (int tip = 0; tip < 6; tip++) {
                int tipY1 = acy - tip;
                int tipY2 = acy + tip;
                int tipX = acx + 4 + tip;
                if (tipY1 >= beltTop && tipY2 < beltBot) {
                    p.drawPixel(tipX, tipY1, COL_ARROW);
                    p.drawPixel(tipX, tipY2, COL_ARROW);
                }
            }
        }

        Texture t = new Texture(p);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        p.dispose();
        return t;
    }

    // =========================================================================
    // ATLAS NASTRO CURVO
    // =========================================================================
    /**
     * C_FRAMES tile affiancati. Ogni frame è una "snapshot" dell'arco ruotato
     * di un passo, creando l'animazione di scorrimento in curva.
     *
     * La curva base connette WEST (lato sinistro) → NORTH (lato superiore).
     * La rotazione in render() copre tutte e 4 le varianti.
     *
     * Layout:
     * - Arco pieno (fascia larga) da WEST a NORTH con raggio ~TILE/2
     * - Strisce seguono l'arco (linee concentriche con gap animato)
     * - Freccia curva a metà arco
     * - Bordi/ombre sui lati di ingresso e uscita per continuità visiva
     */
    private static Texture buildCurveAtlas() {
        int W = TILE * C_FRAMES;
        Pixmap p = new Pixmap(W, TILE, Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);

        // Centro arco nell'angolo bottom-LEFT del tile (origine in top-left, Y verso il
        // basso)
        // Curva base: ingresso da SOUTH (bottom), uscita a EAST (right)
        // Centro = (0, TILE) in coordinate pixel del tile
        float arcCX = 0f;
        float arcCY = TILE;
        int beltInner = 14;
        int beltOuter = 48;
        int beltMid = (beltInner + beltOuter) / 2;

        // L'arco va da 0° (destra = EAST) a -90° (su = NORTH) in math standard
        // Ma con Y invertito: 0° = EAST (right), 90° verso il basso = SOUTH
        // Vogliamo SOUTH→EAST: da 270° a 360° con centro bottom-left
        // In Java: angolo 0° = cos=1,sin=0 → punto a destra del centro
        // Con centro (0, TILE): 0° → (0+r, TILE) = (r, TILE) = lato EAST basso
        // 90° (con sin verso il basso in schermo) → (0, TILE+r) = fuori tile
        // Usiamo angoli negativi: da -90° (NORTH = (0, TILE-r)) a 0° (EAST = (r, TILE))

        double startDeg = -90.0; // uscita NORTH
        double endDeg = 0.0; // ingresso EAST

        int stripeCount = 6;
        double totalArc = endDeg - startDeg; // 90 gradi

        for (int f = 0; f < C_FRAMES; f++) {
            int ox = f * TILE;

            // 1. Sfondo
            fillRect(p, ox, 0, TILE, TILE, COL_BASE);

            // 2. Ombre bordi tile
            fillRect(p, ox, 0, TILE, 2, COL_SHADOW);
            fillRect(p, ox, 0, 2, TILE, COL_SHADOW);

            // 3. Fascia piena dell'arco (pixel per pixel, r da inner a outer)
            for (double deg = startDeg; deg <= endDeg; deg += 0.3) {
                double rad = Math.toRadians(deg);
                double cosA = Math.cos(rad);
                double sinA = Math.sin(rad);
                for (int r = beltInner; r <= beltOuter; r++) {
                    int px = ox + (int) (arcCX + r * cosA);
                    int py = (int) (arcCY + r * sinA); // sinA è negativo → va verso l'alto
                    if (px >= ox && px < ox + TILE && py >= 0 && py < TILE)
                        p.drawPixel(px, py, COL_BASE);
                }
            }

            // 4. Bordi nastro (inner/outer edge)
            drawArcLine(p, ox, arcCX, arcCY, beltInner, 3, startDeg, endDeg, COL_EDGE);
            drawArcLine(p, ox, arcCX, arcCY, beltOuter, 3, startDeg, endDeg, COL_EDGE);

            // 5. Strisce animate (offset angolare per frame)
            double degPerStripe = totalArc / stripeCount;
            double frameOffsetDeg = ((double) f / C_FRAMES) * degPerStripe;

            for (int s = 0; s < stripeCount + 1; s++) {
                double sd = startDeg + (s * degPerStripe - frameOffsetDeg);
                double ed = sd + 3.0; // spessore striscia in gradi
                // Clamp nell'arco
                if (ed < startDeg || sd > endDeg)
                    continue;
                sd = Math.max(sd, startDeg);
                ed = Math.min(ed, endDeg);
                drawArcLine(p, ox, arcCX, arcCY, beltMid, 4, sd, ed, COL_STRIPE);
            }

            // 6. Freccia a metà arco (45° = -45° da start)
            double arrowDeg = -45.0;
            double arrowRad = Math.toRadians(arrowDeg);
            int arrowX = ox + (int) (arcCX + beltMid * Math.cos(arrowRad));
            int arrowY = (int) (arcCY + beltMid * Math.sin(arrowRad));

            // Tangente: perpendicolare al raggio, nel verso del moto
            double tanRad = arrowRad + Math.PI / 2;
            double tx = -Math.cos(tanRad); // inverti per direzione moto
            double ty = -Math.sin(tanRad);

            for (int i = -4; i <= 4; i++) {
                int fx2 = arrowX + (int) (tx * i);
                int fy2 = arrowY + (int) (ty * i);
                if (fx2 >= ox && fx2 < ox + TILE && fy2 >= 0 && fy2 < TILE)
                    p.drawPixel(fx2, fy2, COL_ARROW);
            }

            // 7. Bordi continuità lati ingresso/uscita
            // EAST (destra): colonna destra, nella banda del nastro
            int eastBandY = (int) (arcCY - beltOuter);
            fillRect(p, ox + TILE - 3, eastBandY, 3, beltOuter - beltInner, COL_EDGE);
            // NORTH (sopra): riga superiore
            int northBandX = (int) (arcCX + beltInner);
            fillRect(p, ox + northBandX, 0, beltOuter - beltInner, 3, COL_EDGE);
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

    private static void drawArcLine(Pixmap p, int ox,
            double cx, double cy,
            int radius, int thickness,
            double startDeg, double endDeg,
            int color) {
        p.setColor(color);
        double step = 0.4; // gradi per step
        for (double deg = startDeg; deg <= endDeg; deg += step) {
            double rad = Math.toRadians(deg);
            double cosA = Math.cos(rad);
            double sinA = Math.sin(rad);
            for (int t = -(thickness / 2); t <= (thickness / 2); t++) {
                int px = ox + (int) (cx + (radius + t) * cosA);
                int py = (int) (cy + (radius + t) * sinA);
                if (px >= ox && px < ox + TILE && py >= 0 && py < TILE)
                    p.drawPixel(px, py);
            }
        }
    }

    private static int rgba(float r, float g, float b, float a) {
        int ri = (int) (r * 255) & 0xFF;
        int gi = (int) (g * 255) & 0xFF;
        int bi = (int) (b * 255) & 0xFF;
        int ai = (int) (a * 255) & 0xFF;
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
        connectedEast = grid.hasConveyorAt(gridX + 1, gridY);
        connectedWest = grid.hasConveyorAt(gridX - 1, gridY);
        this.curved = isCurveConnection();
        this.outputDirection = inferOutputDirection(fromDirection);
    }

    private Direction inferOutputDirection(Direction from) {
        int count = countConnections();
        if (count == 0)
            return Direction.EAST;
        if (count == 1) {
            if (connectedEast)
                return Direction.EAST;
            if (connectedNorth)
                return Direction.NORTH;
            if (connectedWest)
                return Direction.WEST;
            return Direction.SOUTH;
        }
        if (count == 2 && from != null) {
            Direction opp = opposite(from);
            if (connectedEast && opp != Direction.EAST)
                return Direction.EAST;
            if (connectedNorth && opp != Direction.NORTH)
                return Direction.NORTH;
            if (connectedWest && opp != Direction.WEST)
                return Direction.WEST;
            if (connectedSouth && opp != Direction.SOUTH)
                return Direction.SOUTH;
        }
        if (connectedEast)
            return Direction.EAST;
        if (connectedNorth)
            return Direction.NORTH;
        if (connectedSouth)
            return Direction.SOUTH;
        return Direction.WEST;
    }

    private static Direction opposite(Direction d) {
        switch (d) {
            case NORTH:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.NORTH;
            case EAST:
                return Direction.WEST;
            case WEST:
                return Direction.EAST;
            default:
                return Direction.EAST;
        }
    }

    // =========================================================================
    // Update / Render
    // =========================================================================
    @Override
    public void update(float delta) {
        /* il tick globale è in Grid.update() */ }

    @Override
    public void render(SpriteBatch batch) {
        if (straightTex == null)
            return;

        float rotation = getRotation();
        float cx = TILE / 2f;
        float cy = TILE / 2f;
        int frame = (int) (globalProgress * (isCurveConnection() ? C_FRAMES : S_FRAMES))
                % (isCurveConnection() ? C_FRAMES : S_FRAMES);

        if (isCurveConnection()) {
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
     * Nastri DRITTI — rotazione basata su outputDirection:
     * 0° → EAST (default atlas, strisce scorrono →)
     * 90° → NORTH (ruotato, strisce scorrono ↑)
     * 180° → WEST (ruotato, strisce scorrono ←)
     * 270° → SOUTH (ruotato, strisce scorrono ↓)
     *
     * Nastri CURVI — atlas base = ingresso WEST, uscita NORTH:
     * 0° → WEST→NORTH
     * 90° → EAST→NORTH
     * 180° → SOUTH→EAST
     * 270° → WEST→SOUTH
     */
    private float getRotation() {
        if (isCurveConnection()) {
            // Atlas base: ingresso SOUTH (bottom), uscita EAST (right)
            if (connectedSouth && connectedEast)
                return 0f;
            if (connectedEast && connectedNorth)
                return 90f; // ruota 90° CCW
            if (connectedNorth && connectedWest)
                return 180f;
            if (connectedWest && connectedSouth)
                return 270f;
            return 0f;
        }
        // nastri dritti invariati
        switch (outputDirection) {
            case EAST:
                return 0f;
            case NORTH:
                return 90f;
            case WEST:
                return 180f;
            case SOUTH:
                return 270f;
            default:
                return 0f;
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    private boolean isCurveConnection() {
        int conn = countConnections();
        if (conn != 2)
            return false;
        boolean straight = (connectedNorth && connectedSouth)
                || (connectedEast && connectedWest);
        return !straight;
    }

    private int countConnections() {
        int c = 0;
        if (connectedNorth)
            c++;
        if (connectedSouth)
            c++;
        if (connectedEast)
            c++;
        if (connectedWest)
            c++;
        return c;
    }

    // Getters
    public boolean isCurved() {
        return curved;
    }

    public Direction getOutputDirection() {
        return outputDirection;
    }

    public boolean isConnectedNorth() {
        return connectedNorth;
    }

    public boolean isConnectedSouth() {
        return connectedSouth;
    }

    public boolean isConnectedEast() {
        return connectedEast;
    }

    public boolean isConnectedWest() {
        return connectedWest;
    }
}