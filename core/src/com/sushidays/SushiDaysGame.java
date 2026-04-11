package com.sushidays;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.sushidays.screens.MainMenuScreen;
import com.sushidays.systems.AudioManager;
import com.sushidays.utils.AssetLoader;
import com.sushidays.utils.GameState;

/**
 * Classe principale del gioco. Estende Game di libGDX.
 * Viene istanziata una volta sola; gestisce il ciclo di vita dell'app.
 */
public class SushiDaysGame extends Game {

    private GameState  gameState;
    private AssetLoader assetLoader;

    @Override
    public void create() {
        // Inizializzazione asset
        assetLoader = AssetLoader.getInstance();
        assetLoader.load();

        // Stato persistente del gioco
        gameState = new GameState();

        // Impostazione volumi audio
        AudioManager.getInstance().setMusicVolume(gameState.musicVolume);
        AudioManager.getInstance().setSfxVolume(gameState.sfxVolume);

        // Prima schermata: menu principale
        setScreen(new MainMenuScreen(this, gameState));
    }

    @Override
    public void render() {
        // Pulisce lo schermo con il colore di sfondo del gioco
        Gdx.gl.glClearColor(0.96f, 0.93f, 0.88f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render(); // delega alla screen corrente
    }

    @Override
    public void dispose() {
        super.dispose();
        assetLoader.dispose();
        AudioManager.getInstance().dispose();
        gameState.save();
    }

    @Override
    public void pause() {
        super.pause();
        gameState.save();
    }

    public GameState getGameState() { return gameState; }
}
