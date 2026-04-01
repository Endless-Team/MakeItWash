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

public class GameOverScreen extends ScreenAdapter {
    private final MainGame game;
    private final Economy economy;
    private final int daysPlayed;
    private Stage stage;
    private SpriteBatch batch;
    private BitmapFont font;
    private UISkin uiSkin;

    public GameOverScreen(MainGame game, Economy economy, int daysPlayed) {
        this.game = game;
        this.economy = economy;
        this.daysPlayed = daysPlayed;
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
        greenButtonStyle.up = uiSkin.getDrawable("assets/ui/Green/Default/button_rectangle_depth_gloss.png");
        greenButtonStyle.down = uiSkin.getDrawable("assets/ui/Green/Default/button_rectangle_gloss.png");
        greenButtonStyle.over = uiSkin.getDrawable("assets/ui/Green/Default/button_rectangle_flat.png");
        skin.add("green_button", greenButtonStyle);

        TextButtonStyle greyButtonStyle = new TextButtonStyle();
        greyButtonStyle.font = font;
        greyButtonStyle.up = uiSkin.getDrawable("assets/ui/Grey/Default/button_rectangle_depth_gloss.png");
        greyButtonStyle.down = uiSkin.getDrawable("assets/ui/Grey/Default/button_rectangle_gloss.png");
        greyButtonStyle.over = uiSkin.getDrawable("assets/ui/Grey/Default/button_rectangle_flat.png");
        skin.add("grey_button", greyButtonStyle);

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        Label titleLabel = new Label("GAME OVER", skin);
        titleLabel.setStyle(labelStyle);
        titleLabel.setColor(new Color(0.9f, 0.3f, 0.3f, 1f));

        Label statsLabel = new Label(String.format("Giorni giocati: %d\nYen finale: %.0f", daysPlayed, economy.getYen()), skin);
        statsLabel.setColor(new Color(0.80f, 0.80f, 0.80f, 1f));

        TextButton restartBtn = new TextButton("Ricomincia", skin, "green_button");
        TextButton mainMenuBtn = new TextButton("Menu Principale", skin, "grey_button");

        restartBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                economy.reset();
                game.setScreen(new MenuScreen(game));
            }
        });

        mainMenuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setScreen(new MenuScreen(game));
            }
        });

        table.add(titleLabel).padBottom(30);
        table.row();
        table.add(statsLabel).padBottom(30);
        table.row();
        table.add(restartBtn).width(250).height(60).padBottom(15);
        table.row();
        table.add(mainMenuBtn).width(250).height(60);

        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.05f, 0.05f, 1f);
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
