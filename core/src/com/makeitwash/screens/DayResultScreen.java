package com.makeitwash.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.makeitwash.MainGame;
import com.makeitwash.world.Economy;
import com.makeitwash.ui.UISkin;

public class DayResultScreen extends ScreenAdapter {
    private final MainGame game;
    private final Economy economy;
    private final int dayNumber;
    private final float earnedYen;
    private Stage stage;
    private SpriteBatch batch;
    private BitmapFont font;
    private UISkin uiSkin;

    public DayResultScreen(MainGame game, Economy economy, int dayNumber, float earnedYen) {
        this.game = game;
        this.economy = economy;
        this.dayNumber = dayNumber;
        this.earnedYen = earnedYen;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        uiSkin = UISkin.get();
        
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/fonts/Roboto-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 32;
        font = generator.generateFont(parameter);
        generator.dispose();

        stage = new Stage();
        
        Skin skin = new Skin();
        skin.add("default", font);
        
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);
        
        TextButtonStyle greenButtonStyle = new TextButtonStyle();
        greenButtonStyle.font = font;
        greenButtonStyle.up = uiSkin.getDrawable("assets/ui/PNG/Green/Default/button_rectangle_depth_gloss.png");
        greenButtonStyle.down = uiSkin.getDrawable("assets/ui/PNG/Green/Default/button_rectangle_gloss.png");
        greenButtonStyle.over = uiSkin.getDrawable("assets/ui/PNG/Green/Default/button_rectangle_flat.png");
        skin.add("green_button", greenButtonStyle);

        TextButtonStyle greyButtonStyle = new TextButtonStyle();
        greyButtonStyle.font = font;
        greyButtonStyle.up = uiSkin.getDrawable("assets/ui/PNG/Grey/Default/button_rectangle_depth_gloss.png");
        greyButtonStyle.down = uiSkin.getDrawable("assets/ui/PNG/Grey/Default/button_rectangle_gloss.png");
        greyButtonStyle.over = uiSkin.getDrawable("assets/ui/PNG/Grey/Default/button_rectangle_flat.png");
        skin.add("grey_button", greyButtonStyle);

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        Label titleLabel = new Label("FINE GIORNATA " + dayNumber, skin);
        titleLabel.setStyle(labelStyle);
        titleLabel.setColor(new Color(0.35f, 0.90f, 0.80f, 1f));

        Label earningsLabel = new Label(String.format("Guadagno: %.0f Yen", earnedYen), skin);
        earningsLabel.setColor(new Color(1.00f, 0.88f, 0.25f, 1f));
        
        Label totalLabel = new Label(String.format("Totale: %.0f Yen", economy.getYen()), skin);
        totalLabel.setColor(new Color(1.00f, 0.88f, 0.25f, 1f));
        
        Label reputationLabel = new Label(String.format("Reputazione: %.0f%%", economy.getReputation()), skin);
        reputationLabel.setColor(new Color(0.35f, 0.90f, 0.80f, 1f));

        TextButton nextDayBtn = new TextButton("Giorno Successivo", skin, "green_button");
        TextButton mainMenuBtn = new TextButton("Menu Principale", skin, "grey_button");

        nextDayBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setScreen(new MenuScreen(game));
            }
        });

        table.add(titleLabel).padBottom(30);
        table.row();
        table.add(earningsLabel).padBottom(10);
        table.row();
        table.add(totalLabel).padBottom(10);
        table.row();
        table.add(reputationLabel).padBottom(30);
        table.row();
        table.add(nextDayBtn).width(250).height(60).padBottom(15);
        table.row();
        table.add(mainMenuBtn).width(250).height(60);

        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.15f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
        font.dispose();
    }
}
