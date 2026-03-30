package com.makeitwash.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.makeitwash.MainGame;
import com.makeitwash.world.Grid;
import com.makeitwash.world.Day;
import com.makeitwash.world.Economy;
import com.makeitwash.ui.HUD;

public class GameScreen extends ScreenAdapter {
    private final MainGame game;
    private final Grid grid;
    private final Day day;
    private final Economy economy;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture gridLineTexture;
    private HUD hud;

    public GameScreen(MainGame game, Grid grid, Day day, Economy economy) {
        this.game = game;
        this.grid = grid;
        this.day = day;
        this.economy = economy;
    }

    public GameScreen(MainGame game, Grid grid, Economy economy) {
        this(game, grid, new Day(), economy);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);
        
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.3f, 0.3f, 0.3f, 1f);
        pixmap.fill();
        gridLineTexture = new Texture(pixmap);
        pixmap.dispose();
        
        hud = new HUD();
        day.start();
    }

    @Override
    public void render(float delta) {
        day.update(delta);
        
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            game.setScreen(new PauseScreen(game, grid, day, economy));
            return;
        }

        if (day.isFinished()) {
            game.setScreen(new DayResultScreen(game, economy, day.getDayNumber(), 0));
            return;
        }

        if (economy.isBankrupt()) {
            game.setScreen(new GameOverScreen(game, economy, day.getDayNumber()));
            return;
        }

        grid.update(delta);

        Gdx.gl.glClearColor(0.12f, 0.14f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.setColor(0.3f, 0.3f, 0.3f, 1f);
        for (int x = 0; x <= Grid.WIDTH; x++) {
            batch.draw(gridLineTexture, x * Grid.CELL_SIZE, 0, 1, Grid.HEIGHT * Grid.CELL_SIZE);
        }
        for (int y = 0; y <= Grid.HEIGHT; y++) {
            batch.draw(gridLineTexture, 0, y * Grid.CELL_SIZE, Grid.WIDTH * Grid.CELL_SIZE, 1);
        }
        batch.end();

        hud.update(economy.getYen(), economy.getReputation(), day.getDayNumber(), day.getTimeRemaining());
        hud.draw();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, 1280, 720);
    }

    @Override
    public void dispose() {
        batch.dispose();
        gridLineTexture.dispose();
        hud.dispose();
    }
}
