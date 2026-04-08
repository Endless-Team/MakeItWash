package com.makeitwash.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.makeitwash.entities.*;
import com.makeitwash.world.*;
import com.makeitwash.ui.UISkin;

import java.util.HashMap;
import java.util.Map;

public class BuildHotbarOverlay extends ScreenAdapter {

    private final Grid    grid;
    private final Economy economy;

    private Stage       stage;
    private SpriteBatch batch;
    private BitmapFont  font;
    private BitmapFont  smallFont;
    private Skin        skin;
    private UISkin      uiSkin;
    private InputAdapter inputAdapter;

    // Hotbar
    private static final int   HOTBAR_SLOTS = 9;
    private final String[]     hotbarIds    = new String[HOTBAR_SLOTS];
    private final String[]     hotbarLabels = new String[HOTBAR_SLOTS];
    private final int[]        hotbarCosts  = new int[HOTBAR_SLOTS];
    private int                activeSlot   = 0;

    private Group trayGroup;
    private Table trayItemTable;
    private Table hotbar;
    private boolean trayOpen = false;

    private static final float HOTBAR_H = 72f;
    private static final float TRAY_H   = 240f;
    private static final float SLOT_W   = 64f;

    // Drag
    private String draggingId    = null;
    private String draggingLabel = null;
    private int    draggingCost  = 0;
    private int    hoverSlot     = -1;

    private Map<String, Texture> itemIcons = new HashMap<>();
    private Image[] slotBackgrounds;

    private Label  errorLabel;
    private float  purchaseErrorTimer = 0f;

    private static Texture placeholderWashing;
    private static Texture placeholderRobot;
    private static boolean placeholdersInitialized = false;

    // ── Catalogo items ────────────────────────────────────────────────────────
    private static final Object[][] ALL_ITEMS = {
        {"lavatrice",    "Lavatrice",    100, "machines"},
        {"asciugatrice", "Asciugatrice", 150, "machines"},
        {"nastro",       "Nastro",        50, "conveyor"},
        {"nastro_curve", "Curva",         60, "conveyor"},
        {"robot",        "Robot",        200, "robots"},
        {"drone",        "Drone",        300, "robots"},
    };

    // ── Ricostruzione ultima direzione di piazzamento (per hintDirection) ─────
    private int lastPlacedGridX = -1;
    private int lastPlacedGridY = -1;

    // =========================================================================
    public BuildHotbarOverlay(Grid grid, Economy economy) {
        this.grid    = grid;
        this.economy = economy;
    }

    @Override
    public void show() {
        batch  = new SpriteBatch();
        uiSkin = UISkin.get();

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
            Gdx.files.internal("assets/fonts/Roboto-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = 16; font = gen.generateFont(p);
        p.size = 13; smallFont = gen.generateFont(p);
        gen.dispose();

        loadItemIcons();

        stage = new Stage(new ScreenViewport());
        skin  = new Skin();
        skin.add("default", font);
        skin.add("small",   smallFont);
        Label.LabelStyle ls = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", ls);
        Label.LabelStyle lsS = new Label.LabelStyle(smallFont, new Color(1f,1f,1f,0.65f));
        skin.add("small", lsS);

        errorLabel = new Label("", skin);
        errorLabel.setColor(Color.RED);
        errorLabel.setVisible(false);
        errorLabel.setWidth(Gdx.graphics.getWidth());
        errorLabel.setAlignment(Align.center);
        errorLabel.setPosition(0, HOTBAR_H + 12f);
        stage.addActor(errorLabel);

        slotBackgrounds = new Image[HOTBAR_SLOTS];
        buildHotbar();
        buildTray();

        inputAdapter = new InputAdapter() {
            @Override
            public boolean keyDown(int k) {
                if (k >= Input.Keys.NUM_1 && k <= Input.Keys.NUM_9) {
                    activeSlot = k - Input.Keys.NUM_1;
                    refreshHotbarVisuals();
                    return true;
                }
                if (k == Input.Keys.B) { toggleTray(); return true; }
                if (k == Input.Keys.ESCAPE) { closeTray(); return true; }
                return false;
            }

            @Override
            public boolean touchDown(int sx, int sy, int ptr, int btn) {
                float wy = Gdx.graphics.getHeight() - sy;
                if (wy > HOTBAR_H + (trayOpen ? TRAY_H : 0)) {
                    placeFromActiveSlot(sx, sy); // passa coords raw, la conversione è interna
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int sx, int sy, int ptr, int btn) {
                if (draggingId != null) {
                    float wy = Gdx.graphics.getHeight() - sy;
                    if (wy <= HOTBAR_H) {
                        int targetSlot = calculateSlotFromX(sx);
                        if (targetSlot >= 0 && targetSlot < HOTBAR_SLOTS) {
                            hotbarIds[targetSlot]    = draggingId;
                            hotbarLabels[targetSlot] = draggingLabel;
                            hotbarCosts[targetSlot]  = draggingCost;
                        }
                    } else {
                        hotbarIds[activeSlot]    = draggingId;
                        hotbarLabels[activeSlot] = draggingLabel;
                        hotbarCosts[activeSlot]  = draggingCost;
                    }
                    draggingId = null;
                    hoverSlot  = -1;
                    refreshHotbarVisuals();
                    return true;
                }
                return false;
            }
        };

        InputMultiplexer mux = new InputMultiplexer(stage, inputAdapter);
        Gdx.input.setInputProcessor(mux);
    }

    // =========================================================================
    // Conversione coordinate schermo → griglia (FIX mouse precision)
    // =========================================================================
    /**
     * Converte le coordinate dello schermo (raw da Gdx.input) in coordinate
     * di griglia usando camera.unproject() per gestire correttamente
     * viewport, zoom e pan.
     *
     * PROBLEMA PRECEDENTE: si usava Gdx.graphics.getHeight()-sy per fare il
     * flip Y e poi si divideva per CELL_SIZE. Questo è corretto SOLO se la
     * camera non ha offset, zoom o letterboxing. Con FitViewport, la viewport
     * può avere bande nere ai lati o sopra/sotto, e la scala pixel→world non
     * è 1:1 → le coordinate erano sbagliate lontano dal centro.
     *
     * SOLUZIONE: usare sempre camera.unproject() che gestisce tutto ciò
     * internamente, poi convertire le coordinate world in coordinate griglia.
     *
     * @param screenX  coordinate schermo X (da Gdx.input.getX() o touchDown sx)
     * @param screenY  coordinate schermo Y (da Gdx.input.getY() o touchDown sy)
     *                 NOTA: LibGDX passa Y già nel formato "0 = alto", NON flippato.
     * @param camera   camera di gioco (non la camera dell'HUD/stage)
     * @return vettore con .x = gridX, .y = gridY (in world coords prima del /TILE)
     */
    private int[] screenToGrid(int screenX, int screenY,
                               com.badlogic.gdx.graphics.OrthographicCamera camera) {
        Vector3 world = new Vector3(screenX, screenY, 0);
        camera.unproject(world); // converte screen→world, gestisce viewport e flip Y
        int gx = (int) Math.floor(world.x / Grid.CELL_SIZE);
        int gy = (int) Math.floor(world.y / Grid.CELL_SIZE);
        return new int[]{ gx, gy };
    }

    // =========================================================================
    // Piazzamento
    // =========================================================================
    private com.badlogic.gdx.graphics.OrthographicCamera gameCamera = null;

    /** Chiamato da GameScreen per fornire la camera di gioco. */
    public void setGameCamera(com.badlogic.gdx.graphics.OrthographicCamera camera) {
        this.gameCamera = camera;
    }

    private void placeFromActiveSlot(int screenX, int screenY) {
        if (hotbarIds[activeSlot] == null || gameCamera == null) return;

        int[] gp = screenToGrid(screenX, screenY, gameCamera);
        int gx = gp[0], gy = gp[1];

        if (!grid.isValid(gx, gy) || !grid.isEmpty(gx, gy)) return;

        int cost = hotbarCosts[activeSlot];
        if (economy.getYen() < cost) { showInsufficientFundsMessage(); return; }
        if (!economy.spendYen(cost)) return;

        String id = hotbarIds[activeSlot];

        // Calcola hintDirection dalla posizione dell'ultimo nastro piazzato
        ConveyorBelt.Direction hint = inferHintDirection(gx, gy);

        PlaceableEntity entity = switch (id) {
            case "lavatrice"    -> new WashingMachine();
            case "asciugatrice" -> new WashingMachine();
            case "nastro", "nastro_curve" -> new ConveyorBelt(id.equals("nastro_curve"));
            case "robot", "drone" -> new Robot();
            default -> null;
        };

        if (entity != null) {
            if (entity instanceof ConveyorBelt) {
                grid.placeConveyor(gx, gy, hint);
            } else {
                grid.place(entity, gx, gy);
            }
            lastPlacedGridX = gx;
            lastPlacedGridY = gy;
            Gdx.app.log("BuildHotbar", "Placed " + id + " at (" + gx + "," + gy + ")");
        }
    }

    /**
     * Inferisce la hintDirection basandosi sulla posizione del nastro appena
     * piazzato rispetto all'ultimo piazzato.
     * Es: se l'ultimo era a (3,2) e ora piazziamo a (4,2), la hint è EAST
     * (il flusso entra da WEST).
     */
    private ConveyorBelt.Direction inferHintDirection(int gx, int gy) {
        if (lastPlacedGridX < 0) return ConveyorBelt.Direction.WEST;
        int dx = gx - lastPlacedGridX;
        int dy = gy - lastPlacedGridY;
        if (dx == 1  && dy == 0) return ConveyorBelt.Direction.WEST;  // stiamo andando ->
        if (dx == -1 && dy == 0) return ConveyorBelt.Direction.EAST;  // stiamo andando <-
        if (dx == 0  && dy == 1) return ConveyorBelt.Direction.SOUTH; // stiamo andando ^
        if (dx == 0  && dy ==-1) return ConveyorBelt.Direction.NORTH; // stiamo andando v
        return ConveyorBelt.Direction.WEST;
    }

    // =========================================================================
    // Preview su griglia (usa la stessa logica screenToGrid per consistenza)
    // =========================================================================
    public void renderPreviewOnGrid(SpriteBatch batch,
                                    com.badlogic.gdx.graphics.OrthographicCamera camera) {
        if (draggingId == null && hotbarIds[activeSlot] == null) return;
        if (batch == null || camera == null) return;

        String previewId = draggingId != null ? draggingId : hotbarIds[activeSlot];

        // Usa screenToGrid con la stessa camera per precisione identica al piazzamento
        int[] gp = screenToGrid(Gdx.input.getX(), Gdx.input.getY(), camera);
        int gridX = gp[0], gridY = gp[1];

        if (!grid.isValid(gridX, gridY)) return;

        float pixelX = grid.toPixelX(gridX);
        float pixelY = grid.toPixelY(gridY);

        Texture previewTex = itemIcons.get(previewId);
        if (previewTex == null) previewTex = getPlaceholderForItem(previewId);
        if (previewTex == null) return;

        Color prev = batch.getColor().cpy();
        Color c = grid.isEmpty(gridX, gridY)
            ? new Color(0.2f, 1f, 0.2f, 0.5f)
            : new Color(1f, 0.2f, 0.2f, 0.5f);
        batch.setColor(c);
        batch.draw(previewTex, pixelX, pixelY, Grid.CELL_SIZE, Grid.CELL_SIZE);
        batch.setColor(prev);
    }

    // =========================================================================
    // UI building (invariato rispetto all'originale, solo refactoring minori)
    // =========================================================================
    private void buildHotbar() {
        float sw     = stage.getViewport().getScreenWidth();
        float totalW = HOTBAR_SLOTS * SLOT_W + 50f;
        hotbar = new Table();
        hotbar.setBackground(makeColorDrawable(new Color(0.11f, 0.12f, 0.18f, 0.95f)));
        hotbar.setBounds((sw - totalW) / 2f, 0, totalW, HOTBAR_H);

        TextButton.TextButtonStyle expandStyle = new TextButton.TextButtonStyle();
        expandStyle.font = font;
        expandStyle.up   = uiSkin.getDrawable("assets/ui/Blue/Default/button_square_gloss.png");
        expandStyle.over = uiSkin.getDrawable("assets/ui/Grey/Default/button_square_gloss.png");
        skin.add("expand", expandStyle);
        TextButton expandBtn = new TextButton("[B]", skin, "expand");
        expandBtn.setName("expandBtn");
        expandBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { toggleTray(); }
        });
        hotbar.add(expandBtn).size(44f, HOTBAR_H).padRight(6f);

        for (int i = 0; i < HOTBAR_SLOTS; i++) {
            final int idx = i;
            Stack cellStack = new Stack();
            cellStack.setName("slot_" + i);

            TextureRegion selBg  = new TextureRegion(uiSkin.getTexture("assets/ui/Blue/Default/button_square_depth_flat.png"));
            TextureRegion hovBg  = new TextureRegion(uiSkin.getTexture("assets/ui/Blue/Default/button_square_gloss.png"));
            TextureRegion emtBg  = new TextureRegion(uiSkin.getTexture("assets/ui/Grey/Default/button_square_flat.png"));

            Image bg = new Image(i == activeSlot
                ? new TextureRegionDrawable(selBg)
                : new TextureRegionDrawable(emtBg));
            slotBackgrounds[i] = bg;
            cellStack.add(bg);

            if (hotbarIds[i] != null) {
                Texture icon = itemIcons.get(hotbarIds[i]);
                if (icon != null) {
                    Table t = new Table();
                    t.add(new Image(icon)).size(50f, 50f).center().pad(11f, 7f, 6f, 7f);
                    cellStack.add(t);
                }
            }

            Table numOverlay = new Table();
            numOverlay.top().left();
            numOverlay.add(new Label(String.valueOf(i + 1), skin, "small")).top().left().pad(3f, 4f, 0, 0);
            cellStack.add(numOverlay);

            cellStack.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent e, float x, float y, int ptr, int btn) {
                    if (draggingId != null) {
                        hotbarIds[idx]    = draggingId;
                        hotbarLabels[idx] = draggingLabel;
                        hotbarCosts[idx]  = draggingCost;
                        draggingId = null; hoverSlot = -1;
                        refreshHotbarVisuals(); return true;
                    }
                    if (btn == Input.Buttons.RIGHT) {
                        hotbarIds[idx] = null; hotbarLabels[idx] = null; hotbarCosts[idx] = 0;
                        refreshHotbarVisuals(); return true;
                    }
                    activeSlot = idx; refreshHotbarVisuals(); return true;
                }
                @Override
                public void enter(InputEvent e, float x, float y, int ptr, Actor from) {
                    hoverSlot = idx;
                    if (slotBackgrounds[idx] != null)
                        slotBackgrounds[idx].setDrawable(draggingId != null
                            ? new TextureRegionDrawable(new TextureRegion(uiSkin.getTexture("assets/ui/Green/Default/button_square_flat.png")))
                            : new TextureRegionDrawable(hovBg));
                }
                @Override
                public void exit(InputEvent e, float x, float y, int ptr, Actor to) {
                    if (hoverSlot == idx) hoverSlot = -1;
                    if (slotBackgrounds[idx] != null)
                        slotBackgrounds[idx].setDrawable(idx == activeSlot
                            ? new TextureRegionDrawable(selBg)
                            : new TextureRegionDrawable(emtBg));
                }
            });
            hotbar.add(cellStack).size(SLOT_W, HOTBAR_H).space(2f);
        }
        stage.addActor(hotbar);
    }

    private void buildTray() {
        float sw     = stage.getViewport().getScreenWidth();
        float totalW = HOTBAR_SLOTS * SLOT_W + 50f;
        float trayX  = (sw - totalW) / 2f;
        trayGroup = new Group();
        trayGroup.setSize(totalW, TRAY_H);
        trayGroup.setPosition(trayX, -TRAY_H);

        Image trayBg = new Image(makeColorDrawable(new Color(0.10f, 0.11f, 0.17f, 0.97f)));
        trayBg.setSize(totalW, TRAY_H);
        trayItemTable = new Table();
        trayItemTable.top().left().pad(12f);
        ScrollPane scroll = new ScrollPane(trayItemTable);
        scroll.setSize(totalW, TRAY_H - 56f);
        scroll.setPosition(0, 0);

        Table tabBar = new Table();
        tabBar.setBackground(makeColorDrawable(new Color(0.12f, 0.13f, 0.20f, 1f)));
        tabBar.setSize(totalW, 52f);
        tabBar.setPosition(0, TRAY_H - 52f);

        String[] tabNames  = {"[M] Macchine", "[C] Nastri", "[R] Robot"};
        String[] tabFilter = {"machines", "conveyor", "robots"};
        for (int i = 0; i < tabNames.length; i++) {
            final String filter = tabFilter[i];
            TextButton.TextButtonStyle ts = new TextButton.TextButtonStyle();
            ts.font = font;
            ts.up   = uiSkin.getDrawable("assets/ui/Blue/Default/button_rectangle_flat.png");
            ts.over = uiSkin.getDrawable("assets/ui/Blue/Default/button_rectangle_gloss.png");
            skin.add("tt" + i, ts);
            TextButton tb = new TextButton(tabNames[i], skin, "tt" + i);
            tb.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent e, Actor a) { fillTrayItems(filter); }
            });
            tabBar.add(tb).expandX().fillX().height(52f);
        }
        trayGroup.addActor(trayBg);
        trayGroup.addActor(tabBar);
        trayGroup.addActor(scroll);
        stage.addActor(trayGroup);
        fillTrayItems("machines");
    }

    private void fillTrayItems(String category) {
        trayItemTable.clear();
        int col = 0;
        for (Object[] item : ALL_ITEMS) {
            if (!item[3].equals(category)) continue;
            final String id    = (String)  item[0];
            final String label = (String)  item[1];
            final int    cost  = (Integer) item[2];

            Table card = new Table();
            card.setBackground(new TextureRegionDrawable(new TextureRegion(
                uiSkin.getTexture("assets/ui/Blue/Default/button_square_depth_flat.png"))));
            card.pad(8f);

            Texture icon = itemIcons.get(id);
            if (icon != null) card.add(new Image(icon)).size(40f, 40f).center().row();
            else {
                Image ph = new Image(makeColorDrawable(new Color(0.4f, 0.4f, 0.5f, 0.8f)));
                card.add(ph).size(40f, 40f).center().row();
            }
            card.add(new Label(label, skin)).center().row();
            Label costLbl = new Label(cost + " \u00a5", skin, "small");
            costLbl.setColor(new Color(1f, 0.85f, 0.3f, 1f));
            card.add(costLbl).center();

            card.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent e, float x, float y, int ptr, int btn) {
                    draggingId = id; draggingLabel = label; draggingCost = cost;
                    card.setBackground(new TextureRegionDrawable(new TextureRegion(
                        uiSkin.getTexture("assets/ui/Green/Default/button_square_depth_flat.png"))));
                    return true;
                }
                @Override
                public void touchUp(InputEvent e, float x, float y, int ptr, int btn) {
                    card.setBackground(new TextureRegionDrawable(new TextureRegion(
                        uiSkin.getTexture("assets/ui/Blue/Default/button_square_depth_flat.png"))));
                    if (draggingId != null) {
                        int ts = hoverSlot >= 0 ? hoverSlot : activeSlot;
                        hotbarIds[ts] = id; hotbarLabels[ts] = label; hotbarCosts[ts] = cost;
                        draggingId = null; hoverSlot = -1;
                        refreshHotbarVisuals();
                    }
                }
            });
            trayItemTable.add(card).size(90f, 80f).pad(6f);
            if (++col % 8 == 0) trayItemTable.row();
        }
    }

    private int calculateSlotFromX(float screenX) {
        float sw     = stage.getViewport().getScreenWidth();
        float totalW = HOTBAR_SLOTS * SLOT_W + 50f;
        float hotbarX = (sw - totalW) / 2f + 50f;
        if (screenX >= hotbarX && screenX < hotbarX + HOTBAR_SLOTS * SLOT_W)
            return (int)((screenX - hotbarX) / SLOT_W);
        return -1;
    }

    private void showInsufficientFundsMessage() {
        purchaseErrorTimer = 2.0f;
        if (errorLabel != null) { errorLabel.setText("Soldi insufficienti"); errorLabel.setVisible(true); }
    }

    public void toggleTray() { if (trayOpen) closeTray(); else openTray(); }

    public void openTray() {
        trayOpen = true;
        trayGroup.addAction(Actions.moveTo(trayGroup.getX(), HOTBAR_H, 0.35f, Interpolation.exp10Out));
    }

    public void closeTray() {
        trayGroup.addAction(Actions.sequence(
            Actions.moveTo(trayGroup.getX(), -TRAY_H, 0.28f, Interpolation.exp5In),
            Actions.run(() -> trayOpen = false)
        ));
    }

    private void refreshHotbarVisuals() { hotbar.remove(); buildHotbar(); }

    private TextureRegionDrawable makeColorDrawable(Color c) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(c); pm.fill();
        Texture t = new Texture(pm); pm.dispose();
        return new TextureRegionDrawable(new TextureRegion(t));
    }

    // =========================================================================
    private void loadItemIcons() {
        try {
            itemIcons.put("lavatrice",    new Texture("isometric_buildings/PNG/buildingTiles_128.png"));
            itemIcons.put("asciugatrice", new Texture("isometric_buildings/PNG/buildingTiles_127.png"));
            itemIcons.put("nastro",       new Texture("isometric_buildings/PNG/conveyor-stripe-sides.png"));
            itemIcons.put("nastro_curve", new Texture("isometric_buildings/PNG/conveyor-stripe.png"));
            itemIcons.put("robot",        new Texture("isometric_buildings/PNG/buildingTiles_080.png"));
            itemIcons.put("drone",        new Texture("isometric_buildings/PNG/buildingTiles_081.png"));
        } catch (Exception e) {
            Gdx.app.log("BuildHotbar", "Error loading icons: " + e.getMessage());
        }
        ensurePlaceholdersLoaded();
    }

    private static void ensurePlaceholdersLoaded() {
        if (placeholdersInitialized) return;
        placeholdersInitialized = true;
        Pixmap pw = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pw.setColor(0.3f, 0.4f, 0.6f, 1f); pw.fill();
        pw.setColor(0.5f, 0.7f, 1f, 1f); pw.drawRectangle(8, 8, 48, 48);
        placeholderWashing = new Texture(pw); pw.dispose();
        Pixmap pr = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pr.setColor(0.6f, 0.4f, 0.2f, 1f); pr.fill();
        pr.setColor(1f, 0.7f, 0.3f, 1f); pr.drawRectangle(8, 8, 48, 48);
        placeholderRobot = new Texture(pr); pr.dispose();
    }

    public static void disposePlaceholders() {
        if (placeholderWashing != null) { placeholderWashing.dispose(); placeholderWashing = null; }
        if (placeholderRobot   != null) { placeholderRobot.dispose();   placeholderRobot   = null; }
        placeholdersInitialized = false;
    }

    private Texture getPlaceholderForItem(String id) {
        return switch (id) {
            case "lavatrice", "asciugatrice" -> placeholderWashing;
            case "robot", "drone"            -> placeholderRobot;
            default                          -> itemIcons.get("nastro");
        };
    }

    // =========================================================================
    @Override
    public void render(float delta) {
        if (stage == null) return;
        stage.act(delta);
        if (purchaseErrorTimer > 0f) {
            purchaseErrorTimer -= delta;
            if (purchaseErrorTimer <= 0f && errorLabel != null) {
                errorLabel.setVisible(false); errorLabel.setText("");
            }
        }
        stage.draw();
        if (draggingId != null && batch != null && font != null) {
            batch.begin();
            font.draw(batch, "[ " + draggingLabel + " ]",
                Gdx.input.getX() - 30, Gdx.graphics.getHeight() - Gdx.input.getY() + 20);
            batch.end();
        }
    }

    @Override
    public void resize(int w, int h) {
        if (stage != null) stage.getViewport().update(w, h, true);
    }

    public Stage       getStage()       { return stage; }
    public InputAdapter getInputAdapter() { return inputAdapter; }

    @Override
    public void dispose() {
        if (stage     != null) stage.dispose();
        if (batch     != null) batch.dispose();
        if (font      != null) font.dispose();
        if (smallFont != null) smallFont.dispose();
        for (Texture t : itemIcons.values()) if (t != null) t.dispose();
        disposePlaceholders();
    }
}