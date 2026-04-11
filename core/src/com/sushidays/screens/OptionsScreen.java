package com.sushidays.screens;


import com.badlogic.gdx.graphics.Color;
import com.sushidays.SushiDaysGame;
import com.sushidays.systems.AudioManager;
import com.sushidays.utils.Constants;
import com.sushidays.utils.GameState;


public class OptionsScreen extends BaseScreen {


    private boolean confirmReset = false;


    private static final Color RED  = new Color(0.85f, 0.20f, 0.15f, 1f);
    private static final Color GRAY = new Color(0.55f, 0.55f, 0.55f, 1f);
    private static final Color BG   = new Color(0.96f, 0.93f, 0.88f, 1f);
    private static final Color DARKRED = new Color(0.60f, 0.10f, 0.08f, 1f);


    public OptionsScreen(SushiDaysGame game, GameState gameState) {
        super(game, gameState);
    }


    @Override
    public void render(float delta) {
        viewport.apply();


        float W = Constants.WORLD_WIDTH;
        float H = Constants.WORLD_HEIGHT;
        float cx = W / 2f;


        fillRect(0, 0, W, H, BG);


        // Header
        fillRect(0, H - 75, W, 75, RED);
        assets.fontLarge.setColor(Color.WHITE);
        drawTextCenteredLarge("OPZIONI", cx, H - 20);
        assets.fontLarge.setColor(Color.WHITE);


        // Pannello
        float panW = 600f, panH = 480f;
        float panX = cx - panW / 2f;
        float panY = H / 2f - panH / 2f - 20;
        fillRect(panX, panY, panW, panH, new Color(0.99f, 0.97f, 0.94f, 1f));
        strokeRect(panX, panY, panW, panH, RED, 2f);


        float labelX  = panX + 40f;
        float ctrlX   = panX + 260f;
        float ctrlW   = 280f;
        float rowH    = 60f;
        float rowY    = panY + panH - 55;


        // --- Volume Musica ---
        assets.fontMedium.setColor(Color.BLACK);
        drawText("Volume Musica", labelX, rowY);
        assets.fontMedium.setColor(DARKRED);
        gameState.musicVolume = drawSlider(ctrlX, rowY - 12, ctrlW, gameState.musicVolume);
        AudioManager.getInstance().setMusicVolume(gameState.musicVolume);
        assets.fontSmall.setColor(GRAY);
        drawText(Math.round(gameState.musicVolume * 100) + "%", ctrlX + ctrlW + 15, rowY);
        assets.fontSmall.setColor(DARKRED);
        rowY -= rowH;


        // --- Volume SFX ---
        assets.fontMedium.setColor(Color.BLACK);
        drawText("Volume Effetti", labelX, rowY);
        assets.fontMedium.setColor(DARKRED);
        gameState.sfxVolume = drawSlider(ctrlX, rowY - 12, ctrlW, gameState.sfxVolume);
        AudioManager.getInstance().setSfxVolume(gameState.sfxVolume);
        assets.fontSmall.setColor(GRAY);
        drawText(Math.round(gameState.sfxVolume * 100) + "%", ctrlX + ctrlW + 15, rowY);
        assets.fontSmall.setColor(DARKRED);
        rowY -= rowH;


        // --- Divisore ---
        fillRect(panX + 20, rowY + 10, panW - 40, 1.5f, new Color(0.80f, 0.80f, 0.80f, 1f));
        rowY -= 15;


        // --- Info ---
        assets.fontSmall.setColor(GRAY);
        drawText("Versione 1.0  •  libGDX 1.12.1  •  Java 17", labelX, rowY);
        assets.fontMedium.setColor(DARKRED);
        drawText("Giorno: " + gameState.currentDay + "  •  Monete: " + gameState.coins
                + "  •  Serviti: " + gameState.totalServed,
                labelX, rowY - 18);
        assets.fontSmall.setColor(DARKRED);
        rowY -= 80; // aumentato da 45 a 80 per più spazio tra info e bottone reset


        // --- Reset ---
        if (!confirmReset) {
            if (drawButton("RESET PROGRESSI", panX + panW / 2f - 180, rowY - 20,
                    360, 50, new Color(0.75f, 0.15f, 0.10f, 1f), Color.WHITE)) {
                confirmReset = true;
            }
        } else {
            assets.fontMedium.setColor(RED);
            drawTextCentered("Sei sicuro? Perderai tutti i progressi!", cx, rowY + 10);
            assets.fontMedium.setColor(Color.WHITE);
            rowY -= 30;
            if (drawButton("SÌ, RESETTA", panX + panW / 2f - 185, rowY - 20,
                    170, 46, RED, Color.WHITE)) {
                gameState.reset();
                confirmReset = false;
                AudioManager.getInstance().playError();
            }
            if (drawButton("ANNULLA", panX + panW / 2f + 15, rowY - 20,
                    170, 46, GRAY, Color.WHITE)) {
                confirmReset = false;
            }
        }


        // Pulsante indietro
        if (drawButton("SALVA E TORNA", cx - 160, panY - 65, 320, 50, RED, Color.WHITE)) {
            gameState.save();
            AudioManager.getInstance().playButton();
            game.setScreen(new MainMenuScreen(game, gameState));
        }
    }


    /**
     * Disegna uno slider orizzontale interattivo e ritorna il valore aggiornato (0..1).
     */
    private float drawSlider(float x, float y, float w, float value) {
        float h    = 8f;
        float knobR = 12f;


        // Track
        fillRect(x, y + h / 2f - 2, w, h, new Color(0.80f, 0.78f, 0.75f, 1f));
        fillRect(x, y + h / 2f - 2, w * value, h, RED);


        // Knob
        float kx = x + w * value;
        fillCircle(kx, y + h / 2f + 2, knobR, RED);
        strokeRect(kx - knobR, y - knobR + h / 2f, knobR * 2, knobR * 2,
                Color.WHITE, 1.5f);


        // Interazione: trascina il knob
        if (com.badlogic.gdx.Gdx.input.isTouched()) {
            float mx = getMouseX();
            float my = getMouseY();
            if (mx >= x - 10 && mx <= x + w + 10 &&
                my >= y - 15  && my <= y + 30) {
                value = Math.max(0f, Math.min(1f, (mx - x) / w));
            }
        }
        return value;
    }
}