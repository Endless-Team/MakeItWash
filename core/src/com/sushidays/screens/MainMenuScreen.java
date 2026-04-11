package com.sushidays.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.sushidays.SushiDaysGame;
import com.sushidays.systems.AudioManager;
import com.sushidays.utils.Constants;
import com.sushidays.utils.GameState;

public class MainMenuScreen extends BaseScreen {

    private static final Color BG      = new Color(0.96f, 0.93f, 0.88f, 1f);
    private static final Color RED     = new Color(0.85f, 0.20f, 0.15f, 1f);
    private static final Color DARKRED = new Color(0.60f, 0.10f, 0.08f, 1f);
    private static final Color GRAY    = new Color(0.60f, 0.60f, 0.60f, 1f);

    // Animazione logo
    private float logoTimer = 0f;

    public MainMenuScreen(SushiDaysGame game, GameState gameState) {
        super(game, gameState);
    }

    @Override
    public void show() {
        AudioManager.getInstance().playMenuMusic();
    }

    @Override
    public void render(float delta) {
        logoTimer += delta;
        viewport.apply();

        float W = Constants.WORLD_WIDTH;
        float H = Constants.WORLD_HEIGHT;
        float cx = W / 2f;

        // ---- Sfondo ----
        fillRect(0, 0, W, H, BG);

        // ---- Decorazione sfondo: strisce colorate ----
        fillRect(0, H - 80, W, 80, RED);
        fillRect(0, 0, W, 50, RED);

        // ---- Logo ----
        float bounce = (float) Math.sin(logoTimer * 2f) * 6f;
        // drawTextCenteredLarge("🍣 SUSHIDAYS 🍣", cx, H - 100 + bounce);
        drawTextCenteredLarge("*** SUSHIDAYS ***", cx, H - 100 + bounce);
        assets.layout.setText(assets.fontMedium, "Il tuo ristorante di sushi ti aspetta!");
        assets.fontMedium.setColor(DARKRED);
        drawTextCentered("Il tuo ristorante di sushi ti aspetta!", cx, H - 145);
        assets.fontMedium.setColor(Color.WHITE);

        // ---- Stato giocatore ----
        String dayInfo   = "Giorno " + gameState.currentDay;
        String coinInfo  = "Monete: " + gameState.coins;
        fillRect(cx - 180, H - 220, 360, 55, new Color(0.85f, 0.80f, 0.75f, 1f));
        strokeRect(cx - 180, H - 220, 360, 55, DARKRED, 2f);
        assets.fontMedium.setColor(DARKRED);
        drawTextCentered(dayInfo + "   |   " + coinInfo, cx, H - 180);
        assets.fontMedium.setColor(Color.WHITE);

        // ---- Pulsanti ----
        float bw = 280f, bh = 55f, bx = cx - bw / 2f;
        float by = H / 2f + 60;

        if (drawButton("GIOCA", bx, by, bw, bh, RED, Color.WHITE)) {
            AudioManager.getInstance().playButton();
            game.setScreen(new MissionScreen(game, gameState));
        }
        by -= 75;
        if (drawButton("NEGOZIO", bx, by, bw, bh, new Color(0.20f, 0.55f, 0.80f, 1f), Color.WHITE)) {
            AudioManager.getInstance().playButton();
            game.setScreen(new ShopScreen(game, gameState));
        }
        by -= 75;
        if (drawButton("OPZIONI", bx, by, bw, bh, new Color(0.40f, 0.40f, 0.40f, 1f), Color.WHITE)) {
            AudioManager.getInstance().playButton();
            game.setScreen(new OptionsScreen(game, gameState));
        }
        by -= 75;

        // Esci solo su desktop
        if (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Desktop) {
            if (drawButton("ESCI", bx, by, bw, bh, GRAY, Color.WHITE)) {
                gameState.save();
                Gdx.app.exit();
            }
        }

        // ---- Footer ----
        assets.fontSmall.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
        drawTextCentered("v1.0 — libGDX", cx, 35);
        assets.fontSmall.setColor(Color.WHITE);
    }
}
