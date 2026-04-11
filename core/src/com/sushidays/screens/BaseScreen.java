package com.sushidays.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.sushidays.SushiDaysGame;
import com.sushidays.utils.AssetLoader;
import com.sushidays.utils.Constants;
import com.sushidays.utils.GameState;

/**
 * Classe base per tutte le schermate del gioco.
 * Fornisce viewport, SpriteBatch, ShapeRenderer e metodi di disegno helper.
 */
public abstract class BaseScreen implements Screen {

    protected final SushiDaysGame game;
    protected final GameState     gameState;
    protected final AssetLoader   assets;

    protected FitViewport    viewport;
    protected SpriteBatch    batch;
    protected ShapeRenderer  shape;

    public BaseScreen(SushiDaysGame game, GameState gameState) {
        this.game      = game;
        this.gameState = gameState;
        this.assets    = AssetLoader.getInstance();
        this.viewport  = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        this.batch     = new SpriteBatch();
        this.shape     = new ShapeRenderer();
    }

    // ---------------------------------------------------------------
    // Ciclo di vita
    // ---------------------------------------------------------------

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void show()   {}
    @Override
    public void pause()  {}
    @Override
    public void resume() {}
    @Override
    public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
    }

    // ---------------------------------------------------------------
    // Helper di disegno — rettangoli (placeholder grafica)
    // ---------------------------------------------------------------

    protected void beginShape(ShapeRenderer.ShapeType type) {
        shape.setProjectionMatrix(viewport.getCamera().combined);
        shape.begin(type);
    }

    /** Disegna un rettangolo pieno con colore dato. */
    protected void fillRect(float x, float y, float w, float h, Color color) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        beginShape(ShapeRenderer.ShapeType.Filled);
        shape.setColor(color);
        shape.rect(x, y, w, h);
        shape.end();
    }

    /** Disegna il bordo di un rettangolo. */
    protected void strokeRect(float x, float y, float w, float h, Color color, float lineWidth) {
        Gdx.gl.glLineWidth(lineWidth);
        beginShape(ShapeRenderer.ShapeType.Line);
        shape.setColor(color);
        shape.rect(x, y, w, h);
        shape.end();
        Gdx.gl.glLineWidth(1f);
    }

    /** Disegna un cerchio pieno. */
    protected void fillCircle(float cx, float cy, float r, Color color) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        beginShape(ShapeRenderer.ShapeType.Filled);
        shape.setColor(color);
        shape.circle(cx, cy, r);
        shape.end();
    }

    /** Disegna una barra di progresso orizzontale. */
    protected void drawProgressBar(float x, float y, float w, float h,
                                   float fraction, Color bgColor, Color fgColor) {
        fillRect(x, y, w, h, bgColor);
        if (fraction > 0) fillRect(x, y, w * Math.min(1f, fraction), h, fgColor);
        strokeRect(x, y, w, h, Color.WHITE, 1.5f);
    }

    // ---------------------------------------------------------------
    // Helper di disegno — testo
    // ---------------------------------------------------------------

    protected void drawText(String text, float x, float y) {
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        assets.fontMedium.draw(batch, text, x, y);
        batch.end();
    }

    protected void drawTextSmall(String text, float x, float y) {
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        assets.fontSmall.draw(batch, text, x, y);
        batch.end();
    }

    protected void drawTextLarge(String text, float x, float y) {
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        assets.fontLarge.draw(batch, text, x, y);
        batch.end();
    }

    protected void drawTextCentered(String text, float cx, float y) {
        assets.layout.setText(assets.fontMedium, text);
        float tw = assets.layout.width;
        drawText(text, cx - tw / 2f, y);
    }

    protected void drawTextCenteredLarge(String text, float cx, float y) {
        assets.layout.setText(assets.fontLarge, text);
        float tw = assets.layout.width;
        drawTextLarge(text, cx - tw / 2f, y);
    }

    // ---------------------------------------------------------------
    // Helper UI — pulsante semplice
    // ---------------------------------------------------------------

    /**
     * Disegna un pulsante e ritorna true se viene cliccato/toccato.
     */
    protected boolean drawButton(String label, float x, float y, float w, float h,
                                 Color bgColor, Color textColor) {
        float mx = getMouseX();
        float my = getMouseY();
        boolean hover = mx >= x && mx <= x + w && my >= y && my <= y + h;

        Color bg = hover ? bgColor.cpy().mul(1.15f, 1.15f, 1.15f, 1f) : bgColor;
        fillRect(x, y, w, h, bg);
        strokeRect(x, y, w, h, Color.WHITE, 1.5f);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        assets.fontMedium.setColor(textColor);
        assets.layout.setText(assets.fontMedium, label);
        float tx = x + (w - assets.layout.width) / 2f;
        float ty = y + (h + assets.layout.height) / 2f;
        assets.fontMedium.draw(batch, label, tx, ty);
        assets.fontMedium.setColor(Color.WHITE);
        batch.end();

        return hover && Gdx.input.justTouched();
    }

    protected boolean drawButton(String label, float x, float y, float w, float h) {
        return drawButton(label, x, y, w, h,
                new Color(0.85f, 0.20f, 0.15f, 1f), Color.WHITE);
    }

    // ---------------------------------------------------------------
    // Input helpers
    // ---------------------------------------------------------------

    /** Coordinate mouse/touch in coordinate del viewport. */
    protected float getMouseX() {
        return viewport.unproject(
                new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)).x;
    }

    protected float getMouseY() {
        return viewport.unproject(
                new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)).y;
    }

    protected boolean isTouched(float x, float y, float w, float h) {
        if (!Gdx.input.justTouched()) return false;
        float mx = getMouseX(), my = getMouseY();
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
