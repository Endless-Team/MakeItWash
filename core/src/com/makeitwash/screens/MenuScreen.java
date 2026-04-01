package com.makeitwash.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.makeitwash.MainGame;
import com.makeitwash.world.Grid;
import com.makeitwash.world.Economy;
import com.makeitwash.ui.UISkin;

public class MenuScreen extends ScreenAdapter {
    private final MainGame game;
    private final Grid grid;
    private final Economy economy;
    private SpriteBatch batch;
    private Stage stage;
    private BitmapFont font;
    private UISkin uiSkin;

    public MenuScreen(MainGame game) {
        this.game = game;
        this.grid = new Grid();
        this.economy = new Economy();
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        uiSkin = UISkin.get();
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/fonts/Roboto-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        font = generator.generateFont(parameter);
        stage = new Stage();
        
        Skin skin = new Skin();
        skin.add("default", font);
        
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = font;
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

        Label titleLabel = new Label("MakeItWash", skin);
        titleLabel.setStyle(labelStyle);
        titleLabel.setColor(new Color(0.35f, 0.90f, 0.80f, 1f));

        TextButton playButton = new TextButton("Inizia Partita", skin, "green_button");
        TextButton quitButton = new TextButton("Esci", skin, "grey_button");

        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setScreen(new GameScreen(game, grid, economy));
            }
        });

        quitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                Gdx.app.exit();
            }
        });

        table.add(titleLabel).padBottom(50);
        table.row();
        table.add(playButton).width(250).height(60).padBottom(15);
        table.row();
        table.add(quitButton).width(250).height(60);

        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Pulisce lo schermo con un colore di sfondo scuro
        Gdx.gl.glClearColor(0.12f, 0.14f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Aggiorna e disegna la UI (stage)
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        // Libera le risorse (batch, stage, font)
        batch.dispose();
        stage.dispose();
        font.dispose();
    }
}
