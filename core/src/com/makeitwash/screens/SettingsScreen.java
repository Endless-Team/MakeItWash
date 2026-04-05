package com.makeitwash.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.Graphics;
import com.makeitwash.MainGame;
import com.makeitwash.ui.UISkin;
import com.makeitwash.world.Economy;
import com.makeitwash.world.Grid;
import com.makeitwash.world.GameSettings;

public class SettingsScreen extends ScreenAdapter {
    private final MainGame game;

    private Stage stage;
    private BitmapFont font;
    private UISkin uiSkin;

    private float masterVolume = 0.8f;
    private float musicVolume = 0.7f;
    private float sfxVolume = 0.85f;
    private float uiScale = 1.0f;

    private boolean fullscreen = false;
    private boolean showGrid = true;
    private boolean pauseOnFocusLost = true;
    private boolean showHints = true;
    private boolean lowPowerMode = false;

    private CheckBox fullscreenBox;

    public SettingsScreen(MainGame game){
        this.game = game;
    }

    @Override
    public void show() {
        uiSkin = UISkin.get();

        // Carica le impostazioni attuali da GameSettings
        GameSettings s = GameSettings.get();
        masterVolume = s.getMasterVolume();
        musicVolume = s.getMusicVolume();
        sfxVolume = s.getSfxVolume();
        uiScale = s.getUiScale();
        fullscreen = s.isFullscreen();
        showGrid = s.isShowGrid();
        pauseOnFocusLost = s.isPauseOnFocusLost();
        showHints = s.isShowHints();
        lowPowerMode = s.isLowPowerMode();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("assets/fonts/Roboto-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 26;
        font = generator.generateFont(parameter);
        generator.dispose();

        // ── skin locale con font assegnato ──────────────────────────────────
        Skin skin = new Skin();

        // LabelStyle di default con font → risolve "Missing LabelStyle font"
        LabelStyle defaultLabel = new LabelStyle();
        defaultLabel.font = font;
        defaultLabel.fontColor = Color.WHITE;
        skin.add("default", defaultLabel);

        // Header label (colore diverso)
        LabelStyle headerLabel = new LabelStyle();
        headerLabel.font = font;
        headerLabel.fontColor = new Color(0.35f, 0.90f, 0.80f, 1f);
        skin.add("header", headerLabel);

        // TextButtons
        TextButtonStyle greenStyle = new TextButtonStyle();
        greenStyle.font = font;
        greenStyle.fontColor = Color.BLACK;
        greenStyle.up = uiSkin.getDrawable("assets/ui/Green/Default/button_rectangle_depth_gloss.png");
        greenStyle.down = uiSkin.getDrawable("assets/ui/Green/Default/button_rectangle_gloss.png");
        greenStyle.over = uiSkin.getDrawable("assets/ui/Green/Default/button_rectangle_flat.png");
        skin.add("green_button", greenStyle);

        TextButtonStyle greyStyle = new TextButtonStyle();
        greyStyle.font = font;
        greyStyle.fontColor = Color.BLACK;
        greyStyle.up = uiSkin.getDrawable("assets/ui/Grey/Default/button_rectangle_depth_gloss.png");
        greyStyle.down = uiSkin.getDrawable("assets/ui/Grey/Default/button_rectangle_gloss.png");
        greyStyle.over = uiSkin.getDrawable("assets/ui/Grey/Default/button_rectangle_flat.png");
        skin.add("grey_button", greyStyle);

        TextButtonStyle redStyle = new TextButtonStyle();
        redStyle.font = font;
        redStyle.fontColor = Color.BLACK;
        redStyle.up = uiSkin.getDrawable("assets/ui/Red/Default/button_rectangle_depth_gloss.png");
        redStyle.down = uiSkin.getDrawable("assets/ui/Red/Default/button_rectangle_gloss.png");
        redStyle.over = uiSkin.getDrawable("assets/ui/Red/Default/button_rectangle_flat.png");
        skin.add("red_button", redStyle);

        // CheckBox con spunta visibile
        CheckBoxStyle checkStyle = new CheckBoxStyle();
        checkStyle.font = font;
        checkStyle.fontColor = Color.WHITE;
        checkStyle.checkboxOff = new TextureRegionDrawable(new TextureRegion(
            uiSkin.getTexture("assets/ui/Green/Default/check_square_grey.png")));
        checkStyle.checkboxOn  = new TextureRegionDrawable(new TextureRegion(
            uiSkin.getTexture("assets/ui/Green/Default/check_square_color_checkmark.png")));
        skin.add("default", checkStyle);

        // Slider
        SliderStyle sliderStyle = new SliderStyle();
        sliderStyle.background = uiSkin.getSkin().get("slider_horizontal_grey", SliderStyle.class).background;
        sliderStyle.knob       = uiSkin.getSkin().get("slider_horizontal_grey", SliderStyle.class).knob;
        skin.add("default-horizontal", sliderStyle);

        // ── layout ──────────────────────────────────────────────────────────
        stage = new Stage();

        Table root = new Table();
        root.setFillParent(true);
        root.pad(30).top();

        // Titolo
        Label title = new Label("IMPOSTAZIONI", skin, "header");
        Label subtitle = new Label("Audio · Grafica · Comfort", skin);
        subtitle.setColor(new Color(0.75f, 0.80f, 0.85f, 1f));

        // ── sezione Audio ────────────────────────────────────────────────────
        Label audioHeader = new Label("── Audio ──", skin, "header");

        Slider masterSlider = new Slider(0f, 1f, 0.01f, false, skin);
        masterSlider.setValue(masterVolume);
        Label masterValue = new Label(pct(masterVolume), skin);
        masterSlider.addListener(e -> { masterVolume = masterSlider.getValue(); masterValue.setText(pct(masterVolume)); return false; });

        Slider musicSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicSlider.setValue(musicVolume);
        Label musicValue = new Label(pct(musicVolume), skin);
        musicSlider.addListener(e -> { musicVolume = musicSlider.getValue(); musicValue.setText(pct(musicVolume)); return false; });

        Slider sfxSlider = new Slider(0f, 1f, 0.01f, false, skin);
        sfxSlider.setValue(sfxVolume);
        Label sfxValue = new Label(pct(sfxVolume), skin);
        sfxSlider.addListener(e -> { sfxVolume = sfxSlider.getValue(); sfxValue.setText(pct(sfxVolume)); return false; });

        // ── sezione Grafica ──────────────────────────────────────────────────
        Label graphicsHeader = new Label("── Grafica ──", skin, "header");

        Slider uiScaleSlider = new Slider(0.75f, 1.5f, 0.01f, false, skin);
        uiScaleSlider.setValue(uiScale);
        Label uiScaleValue = new Label(String.format("%.2fx", uiScale), skin);
        uiScaleSlider.addListener(e -> { uiScale = uiScaleSlider.getValue(); uiScaleValue.setText(String.format("%.2fx", uiScale)); return false; });

        fullscreenBox = new CheckBox("  Schermo intero", skin);
        fullscreenBox.setChecked(fullscreen);
        fullscreenBox.addListener(e -> { fullscreen = fullscreenBox.isChecked(); return false; });

        CheckBox showGridBox = new CheckBox("  Mostra griglia", skin);
        showGridBox.setChecked(showGrid);
        showGridBox.addListener(e -> { showGrid = showGridBox.isChecked(); return false; });

        CheckBox lowPowerBox = new CheckBox("  Risparmio risorse", skin);
        lowPowerBox.setChecked(lowPowerMode);
        lowPowerBox.addListener(e -> { lowPowerMode = lowPowerBox.isChecked(); return false; });

        // ── sezione Comfort ──────────────────────────────────────────────────
        Label comfortHeader = new Label("── Comfort ──", skin, "header");

        CheckBox pauseFocusBox = new CheckBox("  Pausa quando perdi il focus", skin);
        pauseFocusBox.setChecked(pauseOnFocusLost);
        pauseFocusBox.addListener(e -> { pauseOnFocusLost = pauseFocusBox.isChecked(); return false; });

        CheckBox hintsBox = new CheckBox("  Suggerimenti contestuali", skin);
        hintsBox.setChecked(showHints);
        hintsBox.addListener(e -> { showHints = hintsBox.isChecked(); return false; });

        // ── bottoni ──────────────────────────────────────────────────────────
        TextButton applyBtn = new TextButton("Applica", skin, "green_button");
        TextButton resetBtn = new TextButton("Ripristina", skin, "grey_button");
        TextButton backBtn  = new TextButton("Indietro",  skin, "red_button");

        applyBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, com.badlogic.gdx.scenes.scene2d.Actor a) {
                GameSettings s = GameSettings.get();
                s.setMasterVolume(masterVolume);
                s.setMusicVolume(musicVolume);
                s.setSfxVolume(sfxVolume);
                s.setUiScale(uiScale);
                s.setShowGrid(showGrid);
                s.setPauseOnFocusLost(pauseOnFocusLost);
                s.setShowHints(showHints);
                s.setLowPowerMode(lowPowerMode);

                // Fullscreen reale
                if (fullscreen != s.isFullscreen()) {
                    s.setFullscreen(fullscreen);
                    if (fullscreen) {
                        Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
                        Gdx.graphics.setFullscreenMode(mode);
                    } else {
                        Gdx.graphics.setWindowedMode(1280, 720);
                    }
                }
            }
        });

        resetBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, com.badlogic.gdx.scenes.scene2d.Actor a) {
                GameSettings.get().reset();

                masterVolume = GameSettings.get().getMasterVolume();
                musicVolume  = GameSettings.get().getMusicVolume();
                sfxVolume    = GameSettings.get().getSfxVolume();
                uiScale      = GameSettings.get().getUiScale();
                fullscreen       = GameSettings.get().isFullscreen();
                showGrid         = GameSettings.get().isShowGrid();
                pauseOnFocusLost = GameSettings.get().isPauseOnFocusLost();
                showHints        = GameSettings.get().isShowHints();
                lowPowerMode     = GameSettings.get().isLowPowerMode();

                masterSlider.setValue(masterVolume);  masterValue.setText(pct(masterVolume));
                musicSlider.setValue(musicVolume);    musicValue.setText(pct(musicVolume));
                sfxSlider.setValue(sfxVolume);        sfxValue.setText(pct(sfxVolume));
                uiScaleSlider.setValue(uiScale);      uiScaleValue.setText(String.format("%.2fx", uiScale));

                fullscreenBox.setChecked(fullscreen);
                showGridBox.setChecked(showGrid);
                lowPowerBox.setChecked(lowPowerMode);
                pauseFocusBox.setChecked(pauseOnFocusLost);
                hintsBox.setChecked(showHints);
            }
        });

        backBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, com.badlogic.gdx.scenes.scene2d.Actor a) {
                game.setScreen(new MenuScreen(game));
            }
        });

        // ── composizione tabella ─────────────────────────────────────────────
        Table content = new Table();
        content.defaults().pad(8);
        content.top();

        content.add(audioHeader).left().colspan(3).padTop(16); content.row();
        addSliderRow(content, skin, "Volume generale", masterSlider, masterValue);
        addSliderRow(content, skin, "Musica",           musicSlider,  musicValue);
        addSliderRow(content, skin, "Effetti sonori",   sfxSlider,    sfxValue);

        content.add(graphicsHeader).left().colspan(3).padTop(20); content.row();
        addSliderRow(content, skin, "Scala UI", uiScaleSlider, uiScaleValue);
        content.add(fullscreenBox).left().colspan(3); content.row();
        content.add(showGridBox).left().colspan(3);   content.row();
        content.add(lowPowerBox).left().colspan(3);   content.row();

        content.add(comfortHeader).left().colspan(3).padTop(20); content.row();
        content.add(pauseFocusBox).left().colspan(3); content.row();
        content.add(hintsBox).left().colspan(3);      content.row();

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // solo scroll verticale

        Table buttons = new Table();
        buttons.defaults().pad(8);
        buttons.add(applyBtn).width(180).height(55);
        buttons.add(resetBtn).width(180).height(55);
        buttons.add(backBtn).width(180).height(55);

        root.add(title).padBottom(6);      root.row();
        root.add(subtitle).padBottom(20);  root.row();
        root.add(scrollPane).expand().fill().padBottom(16); root.row();
        root.add(buttons).padTop(8);

        stage.addActor(root);
        Gdx.input.setInputProcessor(stage);
    }

    // ── helper ──────────────────────────────────────────────────────────────

    private String pct(float v) {
        return Math.round(v * 100) + "%";
    }

    private void addSliderRow(Table t, Skin skin, String labelText, Slider slider, Label value) {
        t.add(new Label(labelText, skin)).left().width(220);
        t.add(slider).width(340);
        t.add(value).width(65).right();
        t.row();
    }

    // ── lifecycle ───────────────────────────────────────────────────────────

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0.08f, 0.10f, 0.13f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (font  != null) font.dispose();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }
}
