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
import com.makeitwash.world.Economy;

public class GameOverScreen extends ScreenAdapter {
    private final MainGame game;
    private final Economy economy;
    private final int daysPlayed;
    private Stage stage;
    private SpriteBatch batch;
    private BitmapFont font;

    public GameOverScreen(MainGame game, Economy economy, int daysPlayed) {
        this.game = game;
        this.economy = economy;
        this.daysPlayed = daysPlayed;
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

        Label titleLabel = new Label("GAME OVER", skin);
        titleLabel.setStyle(labelStyle);

        Label statsLabel = new Label(String.format("Giorni giocati: %d\nYen finale: %.0f", daysPlayed, economy.getYen()), skin);

        TextButton restartBtn = new TextButton("Ricomincia", skin);
        TextButton mainMenuBtn = new TextButton("Menu Principale", skin);

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
