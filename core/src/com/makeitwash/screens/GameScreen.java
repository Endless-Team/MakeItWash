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

    private final MainGame  game;
    private final Grid      grid;
    private final Day       day;
    private final Economy   economy;

    private SpriteBatch         batch;
    private OrthographicCamera  camera;
    private FitViewport         viewport;
    private Texture             gridLineTexture;
    private HUD                 hud;
    private BuildHotbarOverlay  buildHud;

    // Dimensioni virtuali: la griglia copre esattamente l'area visibile
    private static final float VIRTUAL_WIDTH  = Grid.WIDTH  * Grid.CELL_SIZE; // 1280
    private static final float VIRTUAL_HEIGHT = Grid.HEIGHT * Grid.CELL_SIZE; //  768

    public GameScreen(MainGame game, Grid grid, Day day, Economy economy) {
        this.game    = game;
        this.grid    = grid;
        this.day     = day;
        this.economy = economy;
    }

    public GameScreen(MainGame game, Grid grid, Economy economy) {
        this(game, grid, new Day(), economy);
    }

    // =========================================================================
    // show()
    // =========================================================================
    @Override
    public void show() {
        if (batch != null) return;

        batch = new SpriteBatch();

        // ── Camera ────────────────────────────────────────────────────────────
        // setToOrtho(false, ...) = Y-up (origine in basso a sinistra).
        // La camera viene impostata QUI in show() e poi aggiornata in resize().
        // Non chiamare viewport.apply(true) e setToOrtho() in sequenza senza
        // aggiornare camera.update() → la camera resterebbe disallineata.
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        // La camera punta al centro della griglia
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0f);
        camera.update();

        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply(true); // centra nel frame fisico

        // ── Griglia ───────────────────────────────────────────────────────────
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.3f, 0.3f, 0.3f, 1f);
        pixmap.fill();
        gridLineTexture = new Texture(pixmap);
        gridLineTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();

        // ── Entità ────────────────────────────────────────────────────────────
        ConveyorBelt.ensureTexturesLoaded();

        // ── HUD e overlay ─────────────────────────────────────────────────────
        hud      = new HUD();
        buildHud = new BuildHotbarOverlay(grid, economy);
        buildHud.show();
        buildHud.setGameCamera(camera); // <- necessario per screenToGrid preciso

        // ── Input ─────────────────────────────────────────────────────────────
        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(buildHud.getStage());
        mux.addProcessor(buildHud.getInputAdapter());
        Gdx.input.setInputProcessor(mux);

        day.start();
    }

    // =========================================================================
    // hide()
    // =========================================================================
    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    // =========================================================================
    // render()
    // =========================================================================
    @Override
    public void render(float delta) {

        // ── Navigazione ───────────────────────────────────────────────────────
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new PauseScreen(game, grid, day, economy));
            return;
        }

        // ── Update logica ─────────────────────────────────────────────────────
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

        // ── Clear ─────────────────────────────────────────────────────────────
        Gdx.gl.glClearColor(0.12f, 0.14f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // ── Render world ──────────────────────────────────────────────────────
        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        // Linee griglia
        batch.setColor(0.3f, 0.3f, 0.3f, 1f);
        for (int gx = 0; gx <= Grid.WIDTH; gx++)
            batch.draw(gridLineTexture, gx * Grid.CELL_SIZE, 0, 1, Grid.HEIGHT * Grid.CELL_SIZE);
        for (int gy = 0; gy <= Grid.HEIGHT; gy++)
            batch.draw(gridLineTexture, 0, gy * Grid.CELL_SIZE, Grid.WIDTH * Grid.CELL_SIZE, 1);
        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);

        // Entità
        for (int gx = 0; gx < Grid.WIDTH; gx++)
            for (int gy = 0; gy < Grid.HEIGHT; gy++) {
                var entity = grid.get(gx, gy);
                if (entity != null) entity.render(batch);
            }

        // Preview piazzamento
        if (buildHud != null)
            buildHud.renderPreviewOnGrid(batch, camera);

        batch.end();

        // ── HUD overlay ───────────────────────────────────────────────────────
        hud.update(economy.getYen(), economy.getReputation(),
                   day.getDayNumber(), day.getTimeRemaining());
        hud.draw();
        buildHud.render(delta);
    }

    // =========================================================================
    // resize()
    // =========================================================================
    @Override
    public void resize(int width, int height) {
        // viewport.update() aggiorna le coordinate di rendering fisiche.
        // camera.setToOrtho() va chiamato PER PRIMO se si vuole cambiare
        // le dimensioni virtuali; qui le teniamo fisse, quindi basta update().
        viewport.update(width, height, true);

        // Dopo viewport.update(true), la camera è centrata sulla viewport.
        // Vogliamo però che punti sempre all'angolo (0,0) in basso a sinistra.
        // Reimposta la posizione manualmente:
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0f);
        camera.update();

        if (hud      != null) hud.resize(width, height);
        if (buildHud != null) buildHud.resize(width, height);
    }

    // =========================================================================
    // dispose()
    // =========================================================================
    @Override
    public void dispose() {
        if (batch           != null) batch.dispose();
        if (gridLineTexture != null) gridLineTexture.dispose();
        if (hud             != null) hud.dispose();
        if (buildHud        != null) buildHud.dispose();
        ConveyorBelt.disposeTextures();
    }
}