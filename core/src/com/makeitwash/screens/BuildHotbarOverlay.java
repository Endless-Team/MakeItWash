package com.makeitwash.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.makeitwash.MainGame;
import com.makeitwash.entities.*;
import com.makeitwash.world.*;
import com.makeitwash.ui.UISkin;
import java.util.HashMap;
import java.util.Map;

public class BuildHotbarOverlay extends ScreenAdapter {

    private final MainGame game;
    private final Grid grid;
    private final Economy economy;

    private Stage stage;
    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont smallFont;   // font ridotto per numero shortcut
    private Skin skin;
    private UISkin uiSkin;
    private InputAdapter inputAdapter;

    // Hotbar state
    private static final int HOTBAR_SLOTS = 9;
    private String[]  hotbarIds    = new String[HOTBAR_SLOTS];
    private String[]  hotbarLabels = new String[HOTBAR_SLOTS];
    private int[]     hotbarCosts  = new int[HOTBAR_SLOTS];
    private int activeSlot = 0;

    // tray
    private Group  trayGroup;
    private Table  trayItemTable;
    private Table  hotbar;
    private boolean trayOpen = false;
    private static final float HOTBAR_H = 72f;
    private static final float TRAY_H   = 240f;
    private static final float SLOT_W   = 64f;

    // Drag state
    private String draggingId    = null;
    private String draggingLabel = null;
    private int    draggingCost  = 0;
    private int hoverSlot = -1;
    private float lastMouseY = 0;
    private Map<String, Texture> itemIcons = new HashMap<>();

    private Image[] slotBackgrounds;

    private static final Object[][] ALL_ITEMS = {
        {"lavatrice",   "Lavatrice",     100, "machines"},
        {"asciugatrice","Asciugatrice",  150, "machines"},
        {"nastro",      "Nastro",         50, "conveyor"},
        {"nastro_curve","Curva",          60, "conveyor"},
        {"robot",       "Robot",         200, "robots"},
        {"drone",       "Drone",         300, "robots"},
    };

    public BuildHotbarOverlay(MainGame game, Grid grid, Economy economy) {
        this.game = game;
        this.grid = grid;
        this.economy = economy;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        uiSkin = UISkin.get();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/fonts/Roboto-Regular.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 16;
        font = generator.generateFont(parameter);

        FreeTypeFontGenerator.FreeTypeFontParameter smallParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        smallParam.size = 13;
        smallFont = generator.generateFont(smallParam);

        generator.dispose();

        loadItemIcons();

        stage = new Stage(new ScreenViewport());
        skin  = new Skin();
        skin.add("default", font);
        skin.add("small",   smallFont);

        Label.LabelStyle ls = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", ls);

        Label.LabelStyle lsSmall = new Label.LabelStyle(smallFont, new Color(1f, 1f, 1f, 0.65f));
        skin.add("small", lsSmall);

        slotBackgrounds = new Image[HOTBAR_SLOTS];

        buildHotbar();
        buildTray();

        InputMultiplexer mux = new InputMultiplexer(stage, inputAdapter = new InputAdapter() {
            @Override
            public boolean keyDown(int k) {
                if(k >= Input.Keys.NUM_1 && k <= Input.Keys.NUM_9) {
                    activeSlot = k - Input.Keys.NUM_1;
                    refreshHotbarVisuals();
                    return true;
                }
                if(k == Input.Keys.B) { toggleTray(); return true; }
                if(k == Input.Keys.ESCAPE) { closeTray(); return true; }
                return false;
            }
            @Override
            public boolean mouseMoved(int sx, int sy) {
                lastMouseY = sy;
                return false;
            }
            @Override
            public boolean touchDown(int sx, int sy, int ptr, int btn) {
                lastMouseY = sy;
                float wy = Gdx.graphics.getHeight() - sy;
                if(wy > HOTBAR_H + (trayOpen ? TRAY_H : 0)) {
                    placeFromActiveSlot(sx, wy);
                    return true;
                }
                return false;
            }
            @Override
            public boolean touchUp(int sx, int sy, int ptr, int btn) {
                if(draggingId != null) {
                    float wy = Gdx.graphics.getHeight() - sy;
                    if(wy <= HOTBAR_H) {
                        int targetSlot = calculateSlotFromX(sx);
                        if(targetSlot >= 0 && targetSlot < HOTBAR_SLOTS) {
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
                    hoverSlot = -1;
                    refreshHotbarVisuals();
                    return true;
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(mux);
    }

    private void loadItemIcons() {
        try {
            itemIcons.put("lavatrice",    new Texture("isometric_buildings/PNG/buildingTiles_128.png"));
            itemIcons.put("asciugatrice", new Texture("isometric_buildings/PNG/buildingTiles_127.png"));
            itemIcons.put("nastro",       new Texture("isometric_buildings/PNG/buildingTiles_064.png"));
            itemIcons.put("nastro_curve", new Texture("isometric_buildings/PNG/buildingTiles_063.png"));
            itemIcons.put("robot",        new Texture("isometric_buildings/PNG/buildingTiles_080.png"));
            itemIcons.put("drone",        new Texture("isometric_buildings/PNG/buildingTiles_081.png"));
            Gdx.app.log("BuildHotbar", "Icons loaded: " + itemIcons.size());
        } catch(Exception e) {
            Gdx.app.log("BuildHotbar", "Error loading icons: " + e.getMessage());
        }
    }

    private void buildHotbar() {
        float sw = Gdx.graphics.getWidth();
        hotbar = new Table();
        hotbar.setBackground(makeColorDrawable(new Color(0.11f, 0.12f, 0.18f, 0.95f)));
        float totalW = HOTBAR_SLOTS * SLOT_W + 50f;
        hotbar.setBounds((sw - totalW) / 2f, 0, totalW, HOTBAR_H);

        TextButton.TextButtonStyle expandStyle = new TextButton.TextButtonStyle();
        expandStyle.font = font;
        expandStyle.up   = uiSkin.getDrawable("assets/ui/PNG/Blue/Default/button_square_gloss.png");
        expandStyle.over = uiSkin.getDrawable("assets/ui/PNG/Grey/Default/button_square_gloss.png");
        skin.add("expand", expandStyle);

        TextButton expandBtn = new TextButton(trayOpen ? "▼" : "☰", skin, "expand");
        expandBtn.setName("expandBtn");
        expandBtn.setSize(44f, HOTBAR_H);
        expandBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { toggleTray(); }
        });
        hotbar.add(expandBtn).size(44f, HOTBAR_H).padRight(6f);

        for(int i = 0; i < HOTBAR_SLOTS; i++) {
            final int idx = i;

            Stack cellStack = new Stack();
            cellStack.setName("slot_" + i);

            TextureRegion normalBg = new TextureRegion(uiSkin.getTexture("assets/ui/PNG/Blue/Default/button_square_flat.png"));
            TextureRegion selectedBg = new TextureRegion(uiSkin.getTexture("assets/ui/PNG/Blue/Default/button_square_depth_flat.png"));
            TextureRegion hoverBg = new TextureRegion(uiSkin.getTexture("assets/ui/PNG/Blue/Default/button_square_gloss.png"));
            TextureRegion emptyBg = new TextureRegion(uiSkin.getTexture("assets/ui/PNG/Grey/Default/button_square_flat.png"));

            final Image bg = new Image(
                i == activeSlot 
                    ? new TextureRegionDrawable(selectedBg) 
                    : new TextureRegionDrawable(emptyBg));
            slotBackgrounds[i] = bg;
            cellStack.add(bg);

            if(hotbarIds[i] != null) {
                Texture icon = itemIcons.get(hotbarIds[i]);
                if(icon != null) {
                    Table iconTable = new Table();
                    Image iconImg = new Image(icon);
                    iconTable.add(iconImg).size(50f, 50f).center().pad(11f, 7f, 6f, 7f);
                    cellStack.add(iconTable);
                }
            }

            Table numOverlay = new Table();
            numOverlay.top().left();
            Label numLabel = new Label(String.valueOf(i + 1), skin, "small");
            numOverlay.add(numLabel).top().left().pad(3f, 4f, 0f, 0f);
            cellStack.add(numOverlay);

            cellStack.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent e, float x, float y, int ptr, int btn) {
                    if(draggingId != null) {
                        hotbarIds[idx]    = draggingId;
                        hotbarLabels[idx] = draggingLabel;
                        hotbarCosts[idx]  = draggingCost;
                        draggingId = null;
                        hoverSlot  = -1;
                        refreshHotbarVisuals();
                        return true;
                    }
                    if(btn == Input.Buttons.RIGHT) {
                        hotbarIds[idx]    = null;
                        hotbarLabels[idx] = null;
                        hotbarCosts[idx]  = 0;
                        refreshHotbarVisuals();
                        return true;
                    }
                    activeSlot = idx;
                    refreshHotbarVisuals();
                    return true;
                }

                @Override
                public void enter(InputEvent e, float x, float y, int ptr, Actor from) {
                    hoverSlot = idx;
                    if(hoverSlot >= 0 && slotBackgrounds[hoverSlot] != null) {
                        if(draggingId != null) {
                            slotBackgrounds[hoverSlot].setDrawable(new TextureRegionDrawable(
                                new TextureRegion(uiSkin.getTexture("assets/ui/PNG/Green/Default/button_square_flat.png"))));
                        } else {
                            slotBackgrounds[hoverSlot].setDrawable(new TextureRegionDrawable(hoverBg));
                        }
                    }
                }

                @Override
                public void exit(InputEvent e, float x, float y, int ptr, Actor to) {
                    if(hoverSlot == idx) hoverSlot = -1;
                    if(idx >= 0 && idx < HOTBAR_SLOTS && slotBackgrounds[idx] != null) {
                        slotBackgrounds[idx].setDrawable(
                            idx == activeSlot 
                                ? new TextureRegionDrawable(selectedBg)
                                : new TextureRegionDrawable(emptyBg));
                    }
                }
            });

            hotbar.add(cellStack).size(SLOT_W, HOTBAR_H).space(2f);
        }

        stage.addActor(hotbar);
    }

    private void buildTray() {
        float sw     = Gdx.graphics.getWidth();
        float totalW = HOTBAR_SLOTS * SLOT_W + 50f;
        float trayX  = (sw - totalW) / 2f;

        trayGroup = new Group();
        trayGroup.setSize(totalW, TRAY_H);
        trayGroup.setPosition(trayX, -TRAY_H);

        Image trayBg = new Image(makeColorDrawable(new Color(0.10f, 0.11f, 0.17f, 0.97f)));
        trayBg.setSize(totalW, TRAY_H);
        trayBg.setPosition(0, 0);

        trayItemTable = new Table();
        trayItemTable.top().left().pad(12f);

        ScrollPane scroll = new ScrollPane(trayItemTable);
        scroll.setSize(trayGroup.getWidth(), TRAY_H - 56f);
        scroll.setPosition(0, 0);

        Table tabBar = new Table();
        tabBar.setBackground(makeColorDrawable(new Color(0.12f, 0.13f, 0.20f, 1f)));
        tabBar.setSize(trayGroup.getWidth(), 52f);
        tabBar.setPosition(0, TRAY_H - 52f);

        String[] tabNames  = {"[M] Macchine", "[C] Nastri", "[R] Robot"};
        String[] tabFilter = {"machines", "conveyor", "robots"};
        for(int i = 0; i < tabNames.length; i++) {
            final String filter = tabFilter[i];
            TextButton.TextButtonStyle ts = new TextButton.TextButtonStyle();
            ts.font = font;
            ts.up   = uiSkin.getDrawable("assets/ui/PNG/Blue/Default/button_rectangle_flat.png");
            ts.over = uiSkin.getDrawable("assets/ui/PNG/Blue/Default/button_rectangle_gloss.png");
            skin.add("tray_tab_" + i, ts);
            TextButton tb = new TextButton(tabNames[i], skin, "tray_tab_" + i);
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
        for(Object[] item : ALL_ITEMS) {
            if(!item[3].equals(category)) continue;
            final String id    = (String) item[0];
            final String label = (String) item[1];
            final int    cost  = (Integer) item[2];

            Table card = new Table();
            card.setBackground(new TextureRegionDrawable(
                new TextureRegion(uiSkin.getTexture("assets/ui/PNG/Blue/Default/button_square_depth_flat.png"))));
            card.pad(8f);

            Texture icon = itemIcons.get(id);
            if(icon != null) {
                Image iconImg = new Image(icon);
                card.add(iconImg).size(40f, 40f).center().row();
            }
            Label nameLabel = new Label(label, skin);
            card.add(nameLabel).center().row();
            Label costLabel = new Label(cost + " \u00a5", skin, "small");
            costLabel.setColor(new Color(1f, 0.85f, 0.3f, 1f));
            card.add(costLabel).center();

            card.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent e, float x, float y, int ptr, int btn) {
                    draggingId    = id;
                    draggingLabel = label;
                    draggingCost  = cost;
                    card.setBackground(new TextureRegionDrawable(
                        new TextureRegion(uiSkin.getTexture("assets/ui/PNG/Green/Default/button_square_depth_flat.png"))));
                    return true;
                }
                @Override
                public void touchUp(InputEvent e, float x, float y, int ptr, int btn) {
                    card.setBackground(new TextureRegionDrawable(
                        new TextureRegion(uiSkin.getTexture("assets/ui/PNG/Blue/Default/button_square_depth_flat.png"))));
                    if(draggingId != null) {
                        int targetSlot = hoverSlot >= 0 ? hoverSlot : activeSlot;
                        hotbarIds[targetSlot]    = id;
                        hotbarLabels[targetSlot] = label;
                        hotbarCosts[targetSlot]  = cost;
                        draggingId = null;
                        hoverSlot  = -1;
                        refreshHotbarVisuals();
                    }
                }
            });

            trayItemTable.add(card).size(90f, 80f).pad(6f);
            if(++col % 8 == 0) trayItemTable.row();
        }
    }

    private int calculateSlotFromX(float screenX) {
        float sw     = Gdx.graphics.getWidth();
        float totalW = HOTBAR_SLOTS * SLOT_W + 50f;
        float hotbarX = (sw - totalW) / 2f + 50f; // offset del bottone espandi (44 + 6 pad)
        if(screenX >= hotbarX && screenX < hotbarX + HOTBAR_SLOTS * SLOT_W) {
            return (int)((screenX - hotbarX) / SLOT_W);
        }
        return -1;
    }

    public Grid getGrid() { return grid; }

    private void placeFromActiveSlot(float screenX, float screenY) {
        if(hotbarIds[activeSlot] == null) return;

        int gx = grid.toGridX(screenX);
        int gy = grid.toGridY(screenY);

        if(!grid.isValid(gx, gy) || !grid.isEmpty(gx, gy)) return;

        int cost = hotbarCosts[activeSlot];
        if(economy.getYen() < cost) return;
        if(!economy.spendYen(cost)) return;

        String id = hotbarIds[activeSlot];
        PlaceableEntity entity = null;

        if(id.equals("lavatrice")) {
            entity = new WashingMachine();
        } else if(id.equals("asciugatrice")) {
            entity = new WashingMachine();
        } else if(id.equals("nastro") || id.equals("nastro_curve")) {
            entity = new ConveyorBelt(id.equals("nastro_curve"));
        } else if(id.equals("robot") || id.equals("drone")) {
            entity = new Robot();
        }

        if(entity != null) {
            grid.place(entity, gx, gy);
            Gdx.app.log("BuildHotbar", "Placed " + id + " at (" + gx + "," + gy + ")");
        }
    }

    public void toggleTray() {
        if(trayOpen) closeTray(); else openTray();
    }

    public void openTray() {
        trayOpen = true;
        trayGroup.addAction(Actions.moveTo(trayGroup.getX(), HOTBAR_H, 0.35f, Interpolation.exp10Out));
        updateExpandButton();
    }

    public void closeTray() {
        trayGroup.addAction(
            Actions.sequence(
                Actions.moveTo(trayGroup.getX(), -TRAY_H, 0.28f, Interpolation.exp5In),
                Actions.run(() -> {
                    trayOpen = false;
                    updateExpandButton();
                })
            )
        );
    }

    private void updateExpandButton() {
        Actor btn = hotbar.findActor("expandBtn");
        if(btn instanceof TextButton) {
            ((TextButton) btn).setText(trayOpen ? "\u25bc" : "\u2630");
        }
    }

    private void refreshHotbarVisuals() {
        hotbar.remove();
        buildHotbar();
    }

    private TextureRegionDrawable makeColorDrawable(Color c) {
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(c); p.fill();
        Texture t = new Texture(p); p.dispose();
        return new TextureRegionDrawable(new TextureRegion(t));
    }

    @Override
    public void render(float delta) {
        if(stage == null) return;
        stage.act(delta);
        stage.draw();

        if(draggingId != null && batch != null && font != null) {
            batch.begin();
            font.draw(batch, "[ " + draggingLabel + " ]",
                Gdx.input.getX() - 30,
                Gdx.graphics.getHeight() - Gdx.input.getY() + 20);
            batch.end();
        }
    }

    @Override
    public void resize(int w, int h) {
        if(stage != null) stage.getViewport().update(w, h, true);
    }

    public Stage getStage() { return stage; }

    public InputAdapter getInputAdapter() { return inputAdapter; }

    @Override
    public void dispose() {
        if(stage != null) stage.dispose();
        if(batch != null) batch.dispose();
        if(font  != null) font.dispose();
        if(smallFont != null) smallFont.dispose();
        for(Texture t : itemIcons.values()) { if(t != null) t.dispose(); }
        if(slotBackgrounds != null) {
            for(Image img : slotBackgrounds) {
                if(img != null && img.getDrawable() instanceof TextureRegionDrawable) {
                    TextureRegionDrawable trd = (TextureRegionDrawable) img.getDrawable();
                    if(trd.getRegion().getTexture() != null) {
                        trd.getRegion().getTexture().dispose();
                    }
                }
            }
        }
    }
}