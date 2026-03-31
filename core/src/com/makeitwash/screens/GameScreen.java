package com.makeitwash.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
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
    private BuildHotbarOverlay buildHud;


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
        if (batch != null) return; // guardia contro doppio show()

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.3f, 0.3f, 0.3f, 1f);
        pixmap.fill();
        gridLineTexture = new Texture(pixmap);
        pixmap.dispose();

        hud = new HUD();

        buildHud = new BuildHotbarOverlay(game, grid, economy);
        buildHud.show();

        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(buildHud.getStage());
        mux.addProcessor(buildHud.getInputAdapter());
        Gdx.input.setInputProcessor(mux);

        day.start();
    }


    @Override
    public void hide() {
        // Deregistra il processor quando si cambia schermo
        Gdx.input.setInputProcessor(null);
    }


    @Override
    public void render(float delta) {
        // 1. Transizioni schermo — prima di qualsiasi update
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new PauseScreen(game, grid, day, economy));
            return;
        }

        // 2. Game logic update
        day.update(delta);
        grid.update(delta);

        // 3. Controlli stato — dopo update, prima di render
        if (day.isFinished()) {
            game.setScreen(new DayResultScreen(game, economy, day.getDayNumber(), 0));
            return;
        }

        if (economy.isBankrupt()) {
            game.setScreen(new GameOverScreen(game, economy, day.getDayNumber()));
            return;
        }

        // 4. Rendering mondo
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

        // 5. UI sopra al mondo — HUD poi overlay
        hud.update(economy.getYen(), economy.getReputation(), day.getDayNumber(), day.getTimeRemaining());
        hud.draw();

        buildHud.render(delta); // render overlay — show() NON va mai chiamato qui
    }


    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, 1280, 720);
        if (buildHud != null) buildHud.resize(width, height);
    }


    @Override
    public void dispose() {
        if (batch != null)           batch.dispose();
        if (gridLineTexture != null) gridLineTexture.dispose();
        if (hud != null)             hud.dispose();
        if (buildHud != null)        buildHud.dispose();
    }
}
