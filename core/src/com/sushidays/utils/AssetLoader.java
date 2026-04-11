package com.sushidays.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

/**
 * Loader di asset con texture generate proceduralmente per la versione
 * placeholder.
 * Quando si avranno asset veri, sostituire i metodi createXxxTexture() con
 * AssetManager.load() + AssetManager.get().
 */
public class AssetLoader {

    private static AssetLoader instance;

    // Texture placeholder generate a runtime
    public Texture pixelWhite;
    public Texture pixelBlack;

    // Font
    public BitmapFont fontSmall;
    public BitmapFont fontMedium;
    public BitmapFont fontLarge;
    public GlyphLayout layout;

    // Audio
    public Music musicMenu;
    public Music musicGame;
    public Music musicRush;

    public Sound sfxCoin;
    public Sound sfxCut;
    public Sound sfxBell;
    public Sound sfxError;
    public Sound sfxSuccess;
    public Sound sfxButton;

    private AssetLoader() {
    }

    public static AssetLoader getInstance() {
        if (instance == null)
            instance = new AssetLoader();
        return instance;
    }

    public void load() {
        pixelWhite = createPixel(Color.WHITE);
        pixelBlack = createPixel(Color.BLACK);

        // Carica font Nunito da file .fnt (dimensioni fisse 16, 24, 32)
        if (Gdx.files.internal("fonts/nunito16.fnt").exists()) {
            fontSmall = new BitmapFont(Gdx.files.internal("fonts/nunito16.fnt"));
            fontMedium = new BitmapFont(Gdx.files.internal("fonts/nunito24.fnt"));
            fontLarge = new BitmapFont(Gdx.files.internal("fonts/nunito32.fnt"));
        } else {
            fontSmall = new BitmapFont();
            fontSmall.getData().setScale(0.9f);
            fontMedium = new BitmapFont();
            fontMedium.getData().setScale(1.4f);
            fontLarge = new BitmapFont();
            fontLarge.getData().setScale(2.2f);
        }

        layout = new GlyphLayout();

        // Audio
        musicMenu = tryLoadMusic("sounds/menu.ogg");
        musicGame = tryLoadMusic("sounds/game.ogg");
        musicRush = tryLoadMusic("sounds/rush.ogg");
        sfxCoin = tryLoadSound("sounds/coin.ogg");
        sfxCut = tryLoadSound("sounds/cut.ogg");
        sfxBell = tryLoadSound("sounds/bell.ogg");
        sfxError = tryLoadSound("sounds/error.ogg");
        sfxSuccess = tryLoadSound("sounds/success.ogg");
        sfxButton = tryLoadSound("sounds/button.ogg");
    }

    private Texture createPixel(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private Music tryLoadMusic(String path) {
        if (Gdx.files.internal(path).exists()) {
            return Gdx.audio.newMusic(Gdx.files.internal(path));
        }
        return null;
    }

    private Sound tryLoadSound(String path) {
        if (Gdx.files.internal(path).exists()) {
            return Gdx.audio.newSound(Gdx.files.internal(path));
        }
        return null;
    }

    public void dispose() {
        pixelWhite.dispose();
        pixelBlack.dispose();
        fontSmall.dispose();
        fontMedium.dispose();
        fontLarge.dispose();
        if (musicMenu != null)
            musicMenu.dispose();
        if (musicGame != null)
            musicGame.dispose();
        if (musicRush != null)
            musicRush.dispose();
        if (sfxCoin != null)
            sfxCoin.dispose();
        if (sfxCut != null)
            sfxCut.dispose();
        if (sfxBell != null)
            sfxBell.dispose();
        if (sfxError != null)
            sfxError.dispose();
        if (sfxSuccess != null)
            sfxSuccess.dispose();
        if (sfxButton != null)
            sfxButton.dispose();
        instance = null;
    }
}
