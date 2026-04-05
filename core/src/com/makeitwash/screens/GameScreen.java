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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.makeitwash.MainGame;
import com.makeitwash.world.Grid;
import com.makeitwash.world.Day;
import com.makeitwash.world.Economy;
import com.makeitwash.ui.HUD;
import com.makeitwash.entities.ConveyorBelt;

public class GameScreen extends ScreenAdapter {
    private final MainGame game;
    private final Grid grid;
    private final Day day;
    private final Economy economy;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private Texture gridLineTexture;
    private HUD hud;
    private BuildHotbarOverlay buildHud;

    private static final float VIRTUAL_WIDTH  = Grid.WIDTH  * Grid.CELL_SIZE;  // 1280
    private static final float VIRTUAL_HEIGHT = Grid.HEIGHT * Grid.CELL_SIZE;  // 768

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
        if (batch != null) return;

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply(true); // centra la camera subito

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.3f, 0.3f, 0.3f, 1f);
        pixmap.fill();
        gridLineTexture = new Texture(pixmap);
        gridLineTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();

        ConveyorBelt.ensureTexturesLoaded();

        hud = new HUD();

        buildHud = new BuildHotbarOverlay(grid, economy);
        buildHud.show();

        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(buildHud.getStage());
        mux.addProcessor(buildHud.getInputAdapter());
        Gdx.input.setInputProcessor(mux);

        day.start();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new PauseScreen(game, grid, day, economy));
            return;
        }

        day.update(delta);
        grid.update(delta);

        if (day.isFinished()) {
            game.setScreen(new DayResultScreen(game, economy, day.getDayNumber(), 0));
            return;
        }

        if (economy.isBankrupt()) {
            game.setScreen(new GameOverScreen(game, economy, day.getDayNumber()));
            return;
        }

        Gdx.gl.glClearColor(0.12f, 0.14f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
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
        
        for (int x = 0; x < Grid.WIDTH; x++) {
            for (int y = 0; y < Grid.HEIGHT; y++) {
                var entity = grid.get(x, y);
                if (entity != null) {
                    entity.render(batch);
                }
            }
        }

        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);

        // Renderizza anteprima del modello trascinato sulla griglia
        if (buildHud != null) {
            buildHud.renderPreviewOnGrid(batch, camera);
        }

        batch.end();

        hud.update(economy.getYen(), economy.getReputation(), day.getDayNumber(), day.getTimeRemaining());
        hud.draw();

        buildHud.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // true = centra la camera nel viewport
        camera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        hud.resize(width, height);
        if (buildHud != null) buildHud.resize(width, height);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (gridLineTexture != null) gridLineTexture.dispose();
        if (hud != null) hud.dispose();
        if (buildHud != null) buildHud.dispose();
        ConveyorBelt.disposeTextures();
    }
}
