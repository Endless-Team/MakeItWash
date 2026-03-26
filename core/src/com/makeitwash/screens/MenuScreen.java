package com.makeitwash.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.makeitwash.MainGame;

public class MenuScreen extends ScreenAdapter {
    private final MainGame game;
    private SpriteBatch batch;
    private BitmapFont font;

    public MenuScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont(); // font di default LibGDX
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.12f, 0.14f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "🫧 MakeItWash",
                Gdx.graphics.getWidth() / 2f - 60,
                Gdx.graphics.getHeight() / 2f + 40);
        font.draw(batch, "Premi SPAZIO per iniziare",
                Gdx.graphics.getWidth() / 2f - 90,
                Gdx.graphics.getHeight() / 2f - 20);
        batch.end();

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            game.setScreen(new GameScreen(game));
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}