package com.makeitwash.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class HUD {
    private final Stage stage;
    private final Label yenLabel;
    private final Label reputationLabel;
    private final Label dayLabel;
    private final Label timeLabel;
    private final BitmapFont fontMain;
    private final BitmapFont fontSmall;
    private final Skin skin;
    private final UISkin uiSkin;
    private Image backgroundPanel;

    public HUD() {
        stage = new Stage(new ScreenViewport());
        uiSkin = UISkin.get();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("assets/fonts/Roboto-Regular.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter mainParam =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        mainParam.size = 18;
        mainParam.color = Color.WHITE;
        fontMain = generator.generateFont(mainParam);

        FreeTypeFontGenerator.FreeTypeFontParameter smallParam =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        smallParam.size = 14;
        smallParam.color = new Color(0.80f, 0.80f, 0.80f, 1f);
        fontSmall = generator.generateFont(smallParam);

        generator.dispose();

        skin = new Skin();

        LabelStyle styleMain = new LabelStyle();
        styleMain.font = fontMain;
        skin.add("default", styleMain);

        LabelStyle styleSmall = new LabelStyle();
        styleSmall.font = fontSmall;
        skin.add("small", styleSmall);

        Table table = new Table();
        table.setFillParent(true);
        table.top().left();
        table.pad(16);

        Texture bgTexture = uiSkin.getTexture("assets/ui/Blue/Default/button_rectangle_flat.png");
        if(bgTexture != null) {
            backgroundPanel = new Image(new TextureRegionDrawable(bgTexture));
            backgroundPanel.setSize(160f, 100f);
            backgroundPanel.setPosition(8f, Gdx.graphics.getHeight() - 116f);
            stage.addActor(backgroundPanel);
        }

        dayLabel  = new Label("Giorno 1",   skin, "small");
        timeLabel = new Label("Tempo: 3:00", skin, "small");
        dayLabel.setColor(new Color(0.75f, 0.85f, 1.00f, 1f));
        timeLabel.setColor(new Color(0.80f, 0.80f, 0.80f, 1f));

        yenLabel        = new Label("Yen: 500",       skin);
        reputationLabel = new Label("Reputazione: 50%", skin);
        yenLabel.setColor(new Color(1.00f, 0.88f, 0.25f, 1f));
        reputationLabel.setColor(new Color(0.35f, 0.90f, 0.80f, 1f));

        table.add(dayLabel).align(Align.left).padBottom(2f);
        table.row();
        table.add(timeLabel).align(Align.left).padBottom(10f);
        table.row();
        table.add(yenLabel).align(Align.left).padBottom(4f);
        table.row();
        table.add(reputationLabel).align(Align.left);

        stage.addActor(table);
    }

    public void update(float yen, float reputation, int day, float timeRemaining) {
        yenLabel.setText(String.format("Yen: %.0f", yen));
        reputationLabel.setText(String.format("Reputazione: %.0f%%", reputation));
        dayLabel.setText(String.format("Giorno %d", day));

        int minutes = (int) (timeRemaining / 60);
        int seconds = (int) (timeRemaining % 60);
        timeLabel.setText(String.format("Tempo: %d:%02d", minutes, seconds));
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if(backgroundPanel != null) {
            backgroundPanel.setPosition(8f, height - 116f);
        }
    }

    public void act(float delta) { stage.act(delta); }

    public void draw() { stage.draw(); }

    public void dispose() {
        stage.dispose();
        fontMain.dispose();
        fontSmall.dispose();
        skin.dispose();
        if(backgroundPanel != null && backgroundPanel.getDrawable() instanceof TextureRegionDrawable) {
            TextureRegionDrawable trd = (TextureRegionDrawable) backgroundPanel.getDrawable();
            if(trd.getRegion().getTexture() != null) {
                trd.getRegion().getTexture().dispose();
            }
        }
    }
}