package com.makeitwash.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import com.makeitwash.world.Economy;

public class BuildMenuScreen extends ScreenAdapter {
    private final MainGame game;
    private final Grid grid;
    private final Economy economy;
    private Stage stage;
    private SpriteBatch batch;
    private BitmapFont font;

    public BuildMenuScreen(MainGame game, Grid grid, Economy economy) {
        this.game = game;
        this.grid = grid;
        this.economy = economy;
    }

    private TextureRegionDrawable createColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(texture));
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);

        stage = new Stage();
        
        Skin skin = new Skin();
        skin.add("default", font);
        
        LabelStyle labelStyle = new LabelStyle();
        labelStyle.font = font;
        skin.add("default", labelStyle);
        
        TextButtonStyle buttonStyle = new TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.up = createColorDrawable(Color.DARK_GRAY);
        buttonStyle.down = createColorDrawable(Color.GRAY);
        buttonStyle.over = createColorDrawable(Color.LIGHT_GRAY);
        skin.add("default", buttonStyle);

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.pad(20);

        Label titleLabel = new Label("MENU COSTRUZIONE", skin);
        titleLabel.setStyle(labelStyle);

        TextButton washingMachineBtn = new TextButton("Lavatrice (100 Yen)", skin);
        TextButton conveyorBtn = new TextButton("Nastro Trasportatore (50 Yen)", skin);
        TextButton robotBtn = new TextButton("Robot (200 Yen)", skin);
        TextButton closeBtn = new TextButton("Chiudi (ESC)", skin);

        washingMachineBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                // Place washing machine logic
            }
        });

        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                game.setScreen(new GameScreen(game, grid, economy));
            }
        });

        table.add(titleLabel).colspan(2).padBottom(30);
        table.row();
        table.add(washingMachineBtn).width(300).height(60).padBottom(15);
        table.row();
        table.add(conveyorBtn).width(300).height(60).padBottom(15);
        table.row();
        table.add(robotBtn).width(300).height(60).padBottom(15);
        table.row();
        table.add(closeBtn).width(300).height(60);

        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
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
