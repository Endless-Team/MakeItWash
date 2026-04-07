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
 * ── COORDINATE ──────────────────────────────────────────────────────────────
 * LibGDX world: Y verso l'ALTO. Pixmap: Y verso il BASSO.
 * SpriteBatch.draw() rotation: CCW in world space.
 *
 * ── NASTRO CURVO — forma a sketch ───────────────────────────────────────────
 * Il tile è diviso da un arco di cerchio con centro in (0,0) del Pixmap
 * (angolo top-left) e raggio TILE (= 64px).
 *
 * ZONA A — sopra l'arco (triangolo top-left):
 *   Strisce VERTICALI che scorrono verso il basso (ingresso NORTH in world).
 *
 * ZONA B — sotto l'arco (triangolo bottom-right):
 *   Strisce ORIZZONTALI che scorrono verso destra (uscita EAST in world).
 *
 * In Pixmap "sopra l'arco" significa: per ogni pixel (px, py),
 *   dist = sqrt((px-ox)^2 + py^2) < TILE  → zona A
 *   dist >= TILE                           → zona B
 *
 * Atlas base:  ingresso SOUTH (bottom, world) → uscita EAST (right, world)
 *   In Pixmap (Y down): ingresso dal basso (py=TILE-1) → uscita a destra (px=ox+TILE-1)
 *
 * Rotazioni CCW SpriteBatch:
 *   0°   → SOUTH→EAST
 *   90°  → WEST→SOUTH
 *   180° → NORTH→WEST
 *   270° → EAST→NORTH
 */
public class ConveyorBelt extends PlaceableEntity {

    // ── Animazione globale ───────────────────────────────────────────────────
    private static float globalProgress = 0f;
    private static float globalSpeed    = 1.2f;
    public static void tickGlobal(float delta) {
        globalProgress = (globalProgress + delta * globalSpeed) % 1f;
    }
    public static void setGlobalSpeed(float s) { globalSpeed = s; }
    public static float getGlobalProgress()    { return globalProgress; }

    // ── Direzioni ────────────────────────────────────────────────────────────
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

    // ── Stato flusso ─────────────────────────────────────────────────────────
    private Direction inputDirection  = Direction.WEST;
    private Direction outputDirection = Direction.EAST;
    private boolean connectedNorth, connectedSouth, connectedEast, connectedWest;

    // ── Costanti texture ─────────────────────────────────────────────────────
    public  static final int TILE     = 64;
    private static final int S_FRAMES = 8;
    private static final int C_FRAMES = 8;
    private static final int BELT_INNER = 10;
    private static final int BELT_OUTER = 54;

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final int COL_BASE   = rgba(0.20f, 0.22f, 0.27f, 1f);
    private static final int COL_BELT   = rgba(0.27f, 0.30f, 0.35f, 1f);
    private static final int COL_ZONE_A = rgba(0.18f, 0.20f, 0.25f, 1f); // zona ingresso
    private static final int COL_ZONE_B = rgba(0.22f, 0.25f, 0.30f, 1f); // zona uscita
    private static final int COL_EDGE   = rgba(0.12f, 0.13f, 0.16f, 1f);
    private static final int COL_ARC    = rgba(0.09f, 0.10f, 0.13f, 1f); // linea arco
    private static final int COL_STRIPE = rgba(0.44f, 0.48f, 0.55f, 1f);
    private static final int COL_ARROW  = rgba(0.70f, 0.76f, 0.85f, 1f);
    private static final int COL_SHADOW = rgba(0.09f, 0.10f, 0.12f, 1f);

    // ── Texture statiche ─────────────────────────────────────────────────────
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
    // ATLAS NASTRO CURVO — due zone separate dall'arco
    // =========================================================================
    /**
     * ZONA A (dentro il quarto di cerchio, dist < ARC_R dal corner top-left):
     *   Sfondo COL_ZONE_A + strisce VERTICALI animate (scorrono verso il basso in Pixmap
     *   = verso SOUTH in world = ingresso SOUTH per l'atlas base).
     *   La fascia della banda è nella striscia px in [BELT_INNER, BELT_OUTER] (lato sinistro).
     *
     * ZONA B (fuori dal cerchio, dist >= ARC_R):
     *   Sfondo COL_ZONE_B + strisce ORIZZONTALI animate (scorrono verso destra in Pixmap
     *   = verso EAST in world = uscita EAST).
     *   La fascia è nella striscia py in [BELT_INNER, BELT_OUTER] (lato superiore Pixmap
     *   = lato inferiore world = SOUTH).
     *
     * Atlas base: SOUTH ingresso (px=ox in Pixmap lato sx, fascia px [BELT_INNER..BELT_OUTER])
     *             EAST  uscita  (py=0  in Pixmap lato top, fascia py [BELT_INNER..BELT_OUTER])
     *
     * Aspetta — con Y-up in world:
     *   Pixmap py=0     = world top = NORTH
     *   Pixmap py=TILE  = world bottom = SOUTH
     *   Pixmap px=ox    = world left  = WEST
     *   Pixmap px=ox+TILE = world right = EAST
     *
     * Quindi per atlas base SOUTH→EAST:
     *   Ingresso da SOUTH (world) = Pixmap py=TILE = riga bassa Pixmap
     *     Fascia ingresso: colonne ox + BELT_INNER .. ox + BELT_OUTER, py vicino a TILE
     *   Uscita a EAST (world) = Pixmap px=ox+TILE = colonna destra Pixmap
     *     Fascia uscita: righe BELT_INNER .. BELT_OUTER, px vicino a ox+TILE
     *
     * L'arco ha centro (ox+TILE, TILE) = angolo bottom-right del tile in Pixmap
     * = angolo bottom-right in world (SOUTH-EAST corner).
     * Raggio = TILE = 64px. La curva divide il tile da WEST (py ~ metà) a NORTH (px ~ metà).
     *
     * Zona A (inside arc = in basso-sinistra del Pixmap, cioè vicino al centro arco):
     *   dist_from_corner = sqrt((ox+TILE-px)^2 + (TILE-py)^2)
     *   dist < TILE → zona A (prossima al corner SE, = ingresso SOUTH e uscita EAST)
     *
     * In zona A: strisce che mostrano il flusso da ingresso (SOUTH, px varia) verso uscita (EAST, py varia)
     */
    private static Texture buildCurveAtlas() {
        Pixmap p = new Pixmap(TILE * C_FRAMES, TILE, Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);

        // Centro arco = angolo bottom-right del tile (in Pixmap: px=TILE, py=TILE)
        // La curva separa la fascia che entra da sinistra (WEST, in basso in world)
        // da quella che esce in alto (NORTH, in world).
        // Per ottenere l'effetto dello sketch (arco concavo verso il corner opposto):
        // Centro = (0, TILE) per Pixmap locale (top-left = origin, Y down)
        // → angolo bottom-left Pixmap = angolo bottom-left world
        // Arco da 0° (EAST = destra Pixmap) a -90° (NORTH = top Pixmap)
        // In math con Y down: 0°→(r,0) cioè destra; 90° (Y down)→(0,r) cioè giù
        // Vogliamo: 0°→(r,0) = bordo EAST, -90°→(0,-r) ma con origine (0,TILE):
        // meglio usare origine (0,0) Pixmap e angoli 0→90:
        //   0°  → (r, 0) = lato NORTH del tile (py=0)  = lato NORTH world
        //   90° → (0, r) = lato WEST  del tile (px=ox) = lato WEST world
        // dist_from_origin = sqrt(px_rel^2 + py^2), se < TILE → dentro l'arco
        //
        // ZONA INSIDE (dist < TILE): vicino all'angolo top-left = ingresso WEST / uscita NORTH
        //   → sfondo scuro, strisce diagonali per "connettività visiva"
        //   In realtà è la zona INATTIVA (lo sketch la mostra come sfondo)
        //
        // ZONA OUTSIDE (dist >= TILE): grande area verso bottom-right
        //   → zone A e B con le due direzioni di flusso
        //
        // ----- RILEGGO LO SKETCH -----
        // Lo sketch mostra:
        //   - Strisce VERTICALI a sinistra + freccia verso il basso (ingresso da destra/top)
        //   - Strisce ORIZZONTALI in alto + freccia verso destra (uscita verso destra/top)
        //   - Arco curvo che separa le due zone, concavo verso l'angolo top-left
        //   - Il tile ha l'arco che va dall'angolo bottom-left a quello top-right
        //   - Dentro l'arco (zona grande, in basso a destra): strisce verticali e orizzontali
        //     che si "piegano" verso le rispettive uscite
        //
        // INTERPRETAZIONE FINALE:
        //   Centro arco = (ox+TILE, 0) = angolo TOP-RIGHT del tile (Pixmap)
        //   Raggio = TILE
        //   La curva va da (ox, 0) [top-left] a (ox+TILE, TILE) [bottom-right]
        //   DENTRO l'arco (dist da top-right < TILE): zona ATTIVA
        //   FUORI dall'arco: zona INATTIVA / bordi
        //
        //   Dentro la zona attiva:
        //     - Sotto la metà (py > TILE/2): strisce VERTICALI + fascia ingresso SOUTH
        //     - Sopra la metà (py <= TILE/2): strisce ORIZZONTALI + fascia uscita NORTH
        //     - La separazione delle due sub-zone è la stessa diagonale a 45°

        for (int f = 0; f < C_FRAMES; f++) {
            int ox = f * TILE;

            // Sfondo base
            fillRect(p, ox, 0, TILE, TILE, COL_BASE);

            // Offset animazione per questo frame
            int animOffset = (f * TILE) / C_FRAMES;

            for (int py = 0; py < TILE; py++) {
                for (int relX = 0; relX < TILE; relX++) {
                    int px = ox + relX;

                    // Distanza dall'angolo top-right (relX=TILE, py=0)
                    double dx = TILE - relX;
                    double dy = (double) py;
                    double dist = Math.sqrt(dx * dx + dy * dy);

                    boolean insideArc = dist < TILE;

                    if (!insideArc) {
                        // Zona INATTIVA (fuori dall'arco = angolo top-left)
                        p.drawPixel(px, py, COL_BASE);
                        continue;
                    }

                    // ── Zona ATTIVA (dentro l'arco) ─────────────────────────
                    // Sub-zone: usa la diagonale a 45° per separare le due direzioni
                    // Al di sopra della diagonale (relX + py < TILE): zona NORD (orizzontale)
                    // Al di sotto della diagonale (relX + py >= TILE): zona SUD (verticale)
                    boolean zoneNorth = (relX + py) < TILE; // strisce orizzontali → uscita EAST
                    boolean zoneSouth = !zoneNorth;          // strisce verticali   → ingresso SOUTH

                    // Colore base della zona
                    p.drawPixel(px, py, zoneNorth ? COL_ZONE_A : COL_ZONE_B);

                    // ── Fascia del nastro (bordi della banda) ─────────────────
                    if (zoneNorth) {
                        // Fascia orizzontale: band di py in [BELT_INNER, BELT_OUTER]
                        if (py >= BELT_INNER && py <= BELT_OUTER) {
                            p.drawPixel(px, py, COL_BELT);
                            // Strisce orizzontali animate (scorrono → destra)
                            int strX = ((relX - animOffset) % 10 + 10) % 10;
                            if (strX < 3) p.drawPixel(px, py, COL_STRIPE);
                        }
                        // Bordi fascia
                        if (py == BELT_INNER || py == BELT_INNER + 1 || py == BELT_INNER + 2)
                            p.drawPixel(px, py, COL_EDGE);
                        if (py == BELT_OUTER - 2 || py == BELT_OUTER - 1 || py == BELT_OUTER)
                            p.drawPixel(px, py, COL_EDGE);
                    } else {
                        // Fascia verticale: band di relX in [BELT_INNER, BELT_OUTER]
                        if (relX >= BELT_INNER && relX <= BELT_OUTER) {
                            p.drawPixel(px, py, COL_BELT);
                            // Strisce verticali animate (scorrono ↓ verso il basso)
                            int strY = ((py - animOffset) % 10 + 10) % 10;
                            if (strY < 3) p.drawPixel(px, py, COL_STRIPE);
                        }
                        // Bordi fascia
                        if (relX == BELT_INNER || relX == BELT_INNER + 1 || relX == BELT_INNER + 2)
                            p.drawPixel(px, py, COL_EDGE);
                        if (relX == BELT_OUTER - 2 || relX == BELT_OUTER - 1 || relX == BELT_OUTER)
                            p.drawPixel(px, py, COL_EDGE);
                    }
                }
            }

            // ── Arco di separazione (spesso 3px) ──────────────────────────────
            // Arco con centro top-right (relX=TILE, py=0), raggio TILE
            for (double angle = 90.0; angle <= 180.0; angle += 0.3) {
                double rad = Math.toRadians(angle);
                double cx = TILE + Math.cos(rad) * TILE; // relX del punto sull'arco
                double cy =        Math.sin(rad) * TILE; // py del punto sull'arco
                // Nota: Math.cos(90°..180°) è negativo → cx va da TILE a 0 ✓
                //       Math.sin(90°..180°) è positivo  → cy va da TILE a 0 ✓
                // Ma sin(90)=1 → cy=TILE, sin(180)=0 → cy=0
                // cos(90)=0  → cx=TILE, cos(180)=-1 → cx=0  ✓ (TILE + (-1)*TILE = 0)
                for (int t = -1; t <= 1; t++) {
                    int px = ox + (int) Math.round(cx) + t;
                    int py =      (int) Math.round(cy);
                    if (px >= ox && px < ox + TILE && py >= 0 && py < TILE)
                        p.drawPixel(px, py, COL_ARC);
                    px = ox + (int) Math.round(cx);
                    py =      (int) Math.round(cy) + t;
                    if (px >= ox && px < ox + TILE && py >= 0 && py < TILE)
                        p.drawPixel(px, py, COL_ARC);
                }
            }

            // ── Diagonale 45° tra le due zone (all'interno dell'arco) ─────────
            for (int i = 0; i < TILE; i++) {
                int relX = i, ppy = TILE - 1 - i;
                double dxD = TILE - relX, dyD = (double) ppy;
                if (Math.sqrt(dxD * dxD + dyD * dyD) < TILE) {
                    for (int t = -1; t <= 1; t++) {
                        int px = ox + relX + t;
                        if (px >= ox && px < ox + TILE)
                            p.drawPixel(px, ppy, COL_ARC);
                    }
                }
            }

            // ── Frecce direzionali ────────────────────────────────────────────
            // Freccia zona NORTH (orizzontale → destra), al centro della fascia
            int arrowNY = (BELT_INNER + BELT_OUTER) / 2; // py centrato nella fascia
            int arrowNX = ox + TILE / 4; // abbastanza dentro la zona nord
            drawArrowH(p, arrowNX, arrowNY, 8, COL_ARROW, BELT_INNER, BELT_OUTER);

            // Freccia zona SOUTH (verticale ↓ in Pixmap = scorrimento verso ingresso)
            int arrowSX = ox + (BELT_INNER + BELT_OUTER) / 2;
            int arrowSY = TILE * 3 / 4;
            drawArrowV(p, arrowSX, arrowSY, 8, COL_ARROW, ox, TILE);

            // ── Ombre bordi tile ───────────────────────────────────────────────
            for (int bw = 0; bw < 2; bw++) {
                for (int i = 0; i < TILE; i++) {
                    p.drawPixel(ox + i, bw, COL_SHADOW);          // top
                    p.drawPixel(ox + i, TILE - 1 - bw, COL_SHADOW); // bottom
                    p.drawPixel(ox + bw, i, COL_SHADOW);           // left
                    p.drawPixel(ox + TILE - 1 - bw, i, COL_SHADOW); // right
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

    // ── Connessioni ──────────────────────────────────────────────────────────
    public void updateConnections(Grid grid) {
        connectedNorth = grid.hasConveyorAt(gridX, gridY + 1);
        connectedSouth = grid.hasConveyorAt(gridX, gridY - 1);
        connectedEast  = grid.hasConveyorAt(gridX + 1, gridY);
        connectedWest  = grid.hasConveyorAt(gridX - 1, gridY);
    }

    // ── Flusso ───────────────────────────────────────────────────────────────
    public void applyFlow(Direction from) {
        this.inputDirection = from;
        if (isCurveConnection()) {
            Direction phys = from.opposite();
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

    // ── Update / Render ───────────────────────────────────────────────────────
    @Override public void update(float delta) {}

    @Override
    public void render(SpriteBatch batch) {
        if (straightTex == null) return;
        boolean curve   = isCurveConnection();
        int frames      = curve ? C_FRAMES : S_FRAMES;
        int frame       = (int)(globalProgress * frames) % frames;
        float rot       = getRotation();
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
     * NASTRI CURVI — atlas base: SOUTH ingresso (Pixmap lato basso), EAST uscita (lato destro)
     * Con Y-up world e CCW rotation SpriteBatch:
     *   0°   → SOUTH→EAST   (nessuna rotazione, atlas base)
     *   90°  → WEST→SOUTH   (ruota 90° CCW: il lato destro va in basso, il lato basso va a sinistra)
     *   180° → NORTH→WEST
     *   270° → EAST→NORTH
     */
    private float getRotation() {
        if (isCurveConnection()) {
            Direction in  = inputDirection.opposite();
            Direction out = outputDirection;
            if (in == Direction.SOUTH && out == Direction.EAST)  return   0f;
            if (in == Direction.WEST  && out == Direction.SOUTH) return  90f;
            if (in == Direction.NORTH && out == Direction.WEST)  return 180f;
            if (in == Direction.EAST  && out == Direction.NORTH) return 270f;
            // Fallback fisico
            if (connectedSouth && connectedEast)  return   0f;
            if (connectedWest  && connectedSouth) return  90f;
            if (connectedNorth && connectedWest)  return 180f;
            if (connectedEast  && connectedNorth) return 270f;
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

    // ── Helpers ───────────────────────────────────────────────────────────────
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

    private static void drawArrowV(Pixmap p, int cx, int cy, int len,
                                   int color, int ox, int tileSize) {
        for (int ay = cy - len; ay <= cy + len / 3; ay++)
            for (int ax = cx - 1; ax <= cx + 1; ax++)
                if (ax >= ox && ax < ox + tileSize && ay >= 0 && ay < tileSize)
                    p.drawPixel(ax, ay, color);
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
              |(((int)(b*255)&0xFF)<<8)|((int)(a*255)&0xFF);
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public Direction getInputDirection()  { return inputDirection; }
    public Direction getOutputDirection() { return outputDirection; }
    public boolean isConnectedNorth()     { return connectedNorth; }
    public boolean isConnectedSouth()     { return connectedSouth; }
    public boolean isConnectedEast()      { return connectedEast; }
    public boolean isConnectedWest()      { return connectedWest; }
}