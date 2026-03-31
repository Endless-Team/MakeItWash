// BuildHotbarOverlay.java — Architettura MINECRAFT BOTTOM BAR (Opzione B)
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
import com.makeitwash.world.*;

public class BuildHotbarOverlay extends ScreenAdapter {

    private final MainGame game;
    private final Grid grid;
    private final Economy economy;

    private Stage stage;
    private SpriteBatch batch;
    private BitmapFont font;
    private Skin skin;
    private InputAdapter inputAdapter;

    // Hotbar state
    private static final int HOTBAR_SLOTS = 9;
    private String[]  hotbarIds    = new String[HOTBAR_SLOTS];
    private String[]  hotbarLabels = new String[HOTBAR_SLOTS];
    private int[]     hotbarCosts  = new int[HOTBAR_SLOTS];
    private int activeSlot = 0;

    // Tray
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

    private static final Object[][] ALL_ITEMS = {
        // {id, label, cost, category}
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
        
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/fonts/Roboto-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 20;
        font = generator.generateFont(parameter);
        generator.dispose();

        stage = new Stage(new ScreenViewport());
        skin  = new Skin();
        skin.add("default", font);

        Label.LabelStyle ls = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", ls);

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

    private void buildHotbar() {
        float sw = Gdx.graphics.getWidth();
        hotbar = new Table();
        hotbar.setBackground(makeColorDrawable(new Color(0.11f, 0.12f, 0.18f, 0.95f)));
        float totalW = HOTBAR_SLOTS * SLOT_W + 80f; // 80 per tasto espandi
        hotbar.setBounds((sw - totalW) / 2f, 0, totalW, HOTBAR_H);

        // Bottone espandi tray
        TextButton.TextButtonStyle expandStyle = new TextButton.TextButtonStyle();
        expandStyle.font = font;
        expandStyle.up   = makeColorDrawable(new Color(0.16f, 0.18f, 0.26f, 1f));
        expandStyle.over = makeColorDrawable(new Color(0.26f, 0.34f, 1f, 0.3f));
        skin.add("expand", expandStyle);

        TextButton expandBtn = new TextButton("▲", skin, "expand");
        expandBtn.setSize(70f, HOTBAR_H);
        expandBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { toggleTray(); }
        });
        hotbar.add(expandBtn).size(70f, HOTBAR_H);

        // 9 slot hotbar
        for(int i = 0; i < HOTBAR_SLOTS; i++) {
            final int idx = i;
            Table slot = new Table();
            slot.setName("slot_" + i);
            slot.setBackground(makeColorDrawable(new Color(0.09f, 0.10f, 0.14f, 1f)));

            Label numLabel = new Label(String.valueOf(i + 1), skin);
            slot.add(numLabel).top().left().padLeft(4f).padTop(2f).row();

            // Se slot ha un item
            if(hotbarIds[i] != null) {
                Label itemLabel = new Label(hotbarLabels[i], skin);
                Label costLabel = new Label(hotbarCosts[i] + "¥", skin);
                slot.add(itemLabel).center().row();
                slot.add(costLabel).center();
            }

            // Diventa drop target per il drag
            slot.addListener(new InputListener() {
                @Override
                public void enter(InputEvent e, float x, float y, int ptr, Actor from) {
                    if(draggingId != null) {
                        hoverSlot = idx;
                        slot.setBackground(
                            makeColorDrawable(new Color(0.29f, 0.92f, 0.74f, 0.25f)));
                    }
                }
                @Override
                public void exit(InputEvent e, float x, float y, int ptr, Actor to) {
                    if(draggingId != null && hoverSlot == idx) {
                        hoverSlot = -1;
                    }
                    slot.setBackground(makeColorDrawable(
                        idx == activeSlot
                            ? new Color(1f, 0.7f, 0.28f, 0.25f)
                            : new Color(0.09f, 0.10f, 0.14f, 1f)));
                }
                @Override
                public boolean touchDown(InputEvent e, float x, float y, int ptr, int btn) {
                    if(draggingId != null) {
                        hotbarIds[idx]    = draggingId;
                        hotbarLabels[idx] = draggingLabel;
                        hotbarCosts[idx]  = draggingCost;
                        draggingId = null;
                        hoverSlot = -1;
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
            });

            hotbar.add(slot).size(SLOT_W, HOTBAR_H);
        }

        stage.addActor(hotbar);
    }

    private void buildTray() {
        float sw = Gdx.graphics.getWidth();
        trayGroup = new Group();
        trayGroup.setSize(Math.min(900f, sw * 0.96f), TRAY_H);
        float tx = (sw - trayGroup.getWidth()) / 2f;
        trayGroup.setPosition(tx, -TRAY_H); // nascosto sotto

        Image trayBg = new Image(makeColorDrawable(new Color(0.10f, 0.11f, 0.16f, 0.97f)));
        trayBg.setSize(trayGroup.getWidth(), TRAY_H);

        trayItemTable = new Table();
        trayItemTable.top().left().pad(12f);

        ScrollPane scroll = new ScrollPane(trayItemTable);
        scroll.setSize(trayGroup.getWidth(), TRAY_H - 56f);
        scroll.setPosition(0, 0);

        // Tabs
        Table tabBar = new Table();
        tabBar.setBackground(makeColorDrawable(new Color(0.12f, 0.13f, 0.20f, 1f)));
        tabBar.setSize(trayGroup.getWidth(), 52f);
        tabBar.setPosition(0, TRAY_H - 52f);

        String[] tabNames = {"[M] Macchine", "[C] Nastri", "[R] Robot"};
        String[] tabFilter= {"machines", "conveyor", "robots"};
        for(int i = 0; i < tabNames.length; i++) {
            final String filter = tabFilter[i];
            TextButton.TextButtonStyle ts = new TextButton.TextButtonStyle();
            ts.font = font;
            ts.up   = makeColorDrawable(new Color(0.14f, 0.16f, 0.23f, 1f));
            ts.over = makeColorDrawable(new Color(0.42f, 0.55f, 1f, 0.2f));
            skin.add("tray_tab_" + i, ts);
            TextButton tb = new TextButton(tabNames[i], skin, "tray_tab_" + i);
            final int fi = i;
            tb.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent e, Actor a) {
                    fillTrayItems(filter);
                }
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
            card.setBackground(makeColorDrawable(new Color(0.14f, 0.16f, 0.22f, 1f)));
            card.pad(8f);
            card.add(new Label(label, skin)).center().row();
            card.add(new Label(cost + " ¥", skin)).center();

            card.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent e, float x, float y, int ptr, int btn) {
                    draggingId    = id;
                    draggingLabel = label;
                    draggingCost  = cost;
                    return true;
                }
                @Override
                public void touchUp(InputEvent e, float x, float y, int ptr, int btn) {
                    if(draggingId != null) {
                        int targetSlot = hoverSlot >= 0 ? hoverSlot : activeSlot;
                        hotbarIds[targetSlot]    = id;
                        hotbarLabels[targetSlot] = label;
                        hotbarCosts[targetSlot]  = cost;
                        draggingId = null;
                        hoverSlot = -1;
                        refreshHotbarVisuals();
                    }
                }
            });

            trayItemTable.add(card).size(90f, 80f).pad(6f);
            if(++col % 8 == 0) trayItemTable.row();
        }
    }

    private int calculateSlotFromX(float screenX) {
        float sw = Gdx.graphics.getWidth();
        float totalW = HOTBAR_SLOTS * SLOT_W + 80f;
        float hotbarX = (sw - totalW) / 2f + 70f;
        if(screenX >= hotbarX && screenX < hotbarX + HOTBAR_SLOTS * SLOT_W) {
            return (int)((screenX - hotbarX) / SLOT_W);
        }
        return -1;
    }

    private void placeFromActiveSlot(float wx, float wy) {
        if(hotbarIds[activeSlot] == null) return;
        int cost = hotbarCosts[activeSlot];
        if(economy.getYen() < cost) return;
        if(!economy.spendYen(cost)) return;
    }

    public void toggleTray() {
        if(trayOpen) closeTray(); else openTray();
    }
    public void openTray() {
        trayOpen = true;
        trayGroup.addAction(Actions.moveTo(trayGroup.getX(), HOTBAR_H, 0.35f, Interpolation.exp10Out));
    }
    public void closeTray() {
        trayGroup.addAction(
            Actions.sequence(
                Actions.moveTo(trayGroup.getX(), -TRAY_H, 0.28f, Interpolation.exp5In),
                Actions.run(() -> trayOpen = false)
            )
        );
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

        // Ghost di drag sopra tutto
        if(draggingId != null && batch != null && font != null) {
            batch.begin();
            font.draw(batch, "[" + draggingLabel + "]",
                Gdx.input.getX() - 30,
                Gdx.graphics.getHeight() - Gdx.input.getY() + 20);
            batch.end();
        }
    }

    @Override
    public void resize(int w, int h) { 
        if(stage != null) stage.getViewport().update(w, h, true); 
    }

    public Stage getStage() {
        return stage;
    }

    public InputAdapter getInputAdapter() {
        return inputAdapter;
    }

    @Override
    public void dispose() { 
        if(stage != null) stage.dispose(); 
        if(batch != null) batch.dispose(); 
        if(font != null) font.dispose(); 
    }
}
