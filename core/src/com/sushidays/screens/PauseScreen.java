package com.sushidays.screens;

import com.badlogic.gdx.graphics.Color;
import com.sushidays.SushiDaysGame;
import com.sushidays.systems.AudioManager;
import com.sushidays.utils.Constants;
import com.sushidays.utils.GameState;

/**
 * Schermata di pausa — overlay sopra il GameScreen.
 * Non dispone il GameScreen sottostante.
 */
public class PauseScreen extends BaseScreen {

    private final GameScreen gameScreen; // riferimento per il ripristino

    public PauseScreen(SushiDaysGame game, GameState gameState, GameScreen gameScreen) {
        super(game, gameState);
        this.gameScreen = gameScreen;
    }

    @Override
    public void render(float delta) {
        viewport.apply();

        float W  = Constants.WORLD_WIDTH;
        float H  = Constants.WORLD_HEIGHT;
        float cx = W / 2f;

        // Overlay scuro semi-trasparente
        fillRect(0, 0, W, H, new Color(0f, 0f, 0f, 0.60f));

        // Pannello centrale
        float panW = 380f, panH = 360f;
        float panX = cx - panW / 2f;
        float panY = H / 2f - panH / 2f;
        fillRect(panX, panY, panW, panH, new Color(0.96f, 0.93f, 0.88f, 1f));
        strokeRect(panX, panY, panW, panH, new Color(0.85f, 0.20f, 0.15f, 1f), 3f);

        // Titolo
        assets.fontLarge.setColor(new Color(0.85f, 0.20f, 0.15f, 1f));
        drawTextCenteredLarge("PAUSA", cx, panY + panH - 22);
        assets.fontLarge.setColor(Color.WHITE);

        // Giorno e monete
        assets.fontSmall.setColor(new Color(0.4f, 0.4f, 0.4f, 1f));
        drawTextCentered("Giorno " + gameState.currentDay + "   |   $ " + gameState.coins,
                cx, panY + panH - 65);
        assets.fontSmall.setColor(Color.WHITE);

        // Pulsanti
        float bw = 300f, bh = 54f, bx = cx - bw / 2f;
        float by = panY + panH - 130;

        if (drawButton("RIPRENDI", bx, by, bw, bh,
                new Color(0.20f, 0.70f, 0.30f, 1f), Color.WHITE)) {
            AudioManager.getInstance().playButton();
            game.setScreen(gameScreen);
        }
        by -= 68;
        if (drawButton("OPZIONI", bx, by, bw, bh,
                new Color(0.40f, 0.40f, 0.40f, 1f), Color.WHITE)) {
            AudioManager.getInstance().playButton();
            game.setScreen(new OptionsScreen(game, gameState));
        }
        by -= 68;
        if (drawButton("MENU PRINCIPALE", bx, by, bw, bh,
                new Color(0.85f, 0.20f, 0.15f, 1f), Color.WHITE)) {
            AudioManager.getInstance().playButton();
            gameState.save();
            game.setScreen(new MainMenuScreen(game, gameState));
        }
    }
}
