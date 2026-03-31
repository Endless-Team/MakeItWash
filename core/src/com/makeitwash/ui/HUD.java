package com.makeitwash.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


public class HUD {
    private final Stage stage;
    private final Label yenLabel;
    private final Label reputationLabel;
    private final Label dayLabel;
    private final Label timeLabel;
    private final BitmapFont font;
    private final Skin skin;


    public HUD() {
        stage = new Stage(new ScreenViewport());

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/fonts/Roboto-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 28;
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter);
        generator.dispose();

        skin = new Skin();
        skin.add("default", font);

        LabelStyle style = new LabelStyle();
        style.font = font;
        skin.add("default", style);

        Table table = new Table();
        table.setFillParent(true);
        table.top().left();
        table.pad(20);

        yenLabel = new Label("Yen: 500", skin);
        reputationLabel = new Label("Reputazione: 50%", skin);
        dayLabel = new Label("Giorno 1", skin);
        timeLabel = new Label("Tempo: 3:00", skin);

        yenLabel.setColor(Color.YELLOW);
        reputationLabel.setColor(Color.CYAN);
        dayLabel.setColor(Color.WHITE);
        timeLabel.setColor(Color.WHITE);

        table.add(dayLabel).align(Align.left).padBottom(10);
        table.row();
        table.add(yenLabel).align(Align.left).padBottom(10);
        table.row();
        table.add(reputationLabel).align(Align.left).padBottom(10);
        table.row();
        table.add(timeLabel).align(Align.left);

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
    }

    public void act(float delta) {
        stage.act(delta);
    }

    public void draw() {
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
        font.dispose();
        skin.dispose();
    }
}