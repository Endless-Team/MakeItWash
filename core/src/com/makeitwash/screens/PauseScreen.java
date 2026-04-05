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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.makeitwash.MainGame;
import com.makeitwash.world.Grid;
import com.makeitwash.world.Day;
import com.makeitwash.world.Economy;
import com.makeitwash.ui.UISkin;

public class PauseScreen extends ScreenAdapter {
    private final MainGame game;
    private final Grid grid;
    private final Day day;
    private final Economy economy;
    private Stage stage;
    private SpriteBatch batch;
    private BitmapFont font;
    private UISkin uiSkin;

    public PauseScreen(MainGame game, Grid grid, Day day, Economy economy) {
        this.game = game;
        this.grid = grid;
        this.day = day;
        this.economy = economy;
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
        greenButtonStyle.fontColor = Color.BLACK;
        greenButtonStyle.up = uiSkin.getDrawable("assets/ui/Green/Default/button_rectangle_depth_gloss.png");
        greenButtonStyle.down = uiSkin.getDrawable("assets/ui/Green/Default/button_rectangle_gloss.png");
        greenButtonStyle.over = uiSkin.getDrawable("assets/ui/Green/Default/button_rectangle_flat.png");
        skin.add("green_button", greenButtonStyle);

        TextButtonStyle greyButtonStyle = new TextButtonStyle();
        greyButtonStyle.font = font;
        greyButtonStyle.fontColor = Color.BLACK;
        greyButtonStyle.up = uiSkin.getDrawable("assets/ui/Grey/Default/button_rectangle_depth_gloss.png");
        greyButtonStyle.down = uiSkin.getDrawable("assets/ui/Grey/Default/button_rectangle_gloss.png");
        greyButtonStyle.over = uiSkin.getDrawable("assets/ui/Grey/Default/button_rectangle_flat.png");
        skin.add("grey_button", greyButtonStyle);

        TextButtonStyle redButtonStyle = new TextButtonStyle();
        redButtonStyle.font = font;
        redButtonStyle.fontColor = Color.BLACK;
        redButtonStyle.up = uiSkin.getDrawable("assets/ui/Red/Default/button_rectangle_depth_gloss.png");
        redButtonStyle.down = uiSkin.getDrawable("assets/ui/Red/Default/button_rectangle_gloss.png");
        redButtonStyle.over = uiSkin.getDrawable("assets/ui/Red/Default/button_rectangle_flat.png");
        skin.add("red_button", redButtonStyle);

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        Label titleLabel = new Label("PAUSA", skin);
        titleLabel.setStyle(labelStyle);
        titleLabel.setColor(new Color(0.35f, 0.90f, 0.80f, 1f));

        TextButton resumeBtn = new TextButton("Riprendi", skin, "green_button");
        TextButton settingsBtn = new TextButton("Impostazioni", skin, "grey_button");
        TextButton mainMenuBtn = new TextButton("Menu Principale", skin, "red_button");

        resumeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setScreen(new GameScreen(game, grid, day, economy));
            }
        });

        settingsBtn.addListener(new ChangeListener(){

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new SettingsScreen(game));
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
        table.add(resumeBtn).width(250).height(60).padBottom(15);
        table.row();
        table.add(settingsBtn).width(250).height(60).padBottom(15);
        table.row();
        table.add(mainMenuBtn).width(250).height(60);

        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
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
