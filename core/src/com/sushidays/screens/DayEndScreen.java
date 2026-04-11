package com.sushidays.screens;

import com.badlogic.gdx.graphics.Color;
import com.sushidays.SushiDaysGame;
import com.sushidays.systems.AudioManager;
import com.sushidays.systems.DayManager;
import com.sushidays.utils.Constants;
import com.sushidays.utils.GameState;

public class DayEndScreen extends BaseScreen {

    private final DayManager dayManager;

    private float animTimer = 0f;

    private static final Color RED   = new Color(0.85f, 0.20f, 0.15f, 1f);
    private static final Color BLUE  = new Color(0.20f, 0.50f, 0.80f, 1f);
    private static final Color GREEN = new Color(0.20f, 0.70f, 0.30f, 1f);
    private static final Color GOLD  = new Color(0.95f, 0.78f, 0.10f, 1f);
    private static final Color DARK  = new Color(0.15f, 0.12f, 0.10f, 1f);
    private static final Color BG    = new Color(0.96f, 0.93f, 0.88f, 1f);

    public DayEndScreen(SushiDaysGame game, GameState gameState, DayManager dayManager) {
        super(game, gameState);
        this.dayManager = dayManager;
        AudioManager.getInstance().stopMusic();
    }

    @Override
    public void show() {
        AudioManager.getInstance().playSuccess();
    }

    @Override
    public void render(float delta) {
        animTimer += delta;
        viewport.apply();

        float W = Constants.WORLD_WIDTH;
        float H = Constants.WORLD_HEIGHT;
        float cx = W / 2f;

        fillRect(0, 0, W, H, BG);

        // Banner superiore
        fillRect(0, H - 90, W, 90, RED);
        assets.fontLarge.setColor(Color.WHITE);
        drawTextCenteredLarge("Fine Giornata — Giorno " + gameState.currentDay, cx, H - 20);
        assets.fontLarge.setColor(Color.WHITE);

        // Pannello riepilogo
        float panW = 700f, panH = 440f;
        float panX = cx - panW / 2f;
        float panY = H / 2f - panH / 2f - 20;
        fillRect(panX, panY, panW, panH, new Color(0.99f, 0.97f, 0.94f, 1f));
        strokeRect(panX, panY, panW, panH, RED, 2f);

        // --- Statistiche ---
        float col1X = panX + 40f;
        float col2X = panX + panW / 2f + 20f;
        float rowH  = 52f;
        float rowY  = panY + panH - 55;

        // Titoli colonne
        assets.fontSmall.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
        drawText("STATISTICHE GIORNATA", col1X, rowY);
        drawText("GUADAGNI", col2X, rowY);
        assets.fontSmall.setColor(Color.WHITE);
        rowY -= 30;
        fillRect(panX + 20, rowY + 8, panW - 40, 1.5f, new Color(0.8f, 0.8f, 0.8f, 1f));
        rowY -= 10;

        // Clienti serviti
        drawStatRow("Clienti serviti", String.valueOf(dayManager.customersServed), GREEN, col1X, rowY);
        drawStatRow("Guadagno base", "$ " + dayManager.coinsEarned, GOLD, col2X, rowY);
        rowY -= rowH;

        drawStatRow("Clienti persi", String.valueOf(dayManager.customersLost),
                dayManager.customersLost > 0 ? RED : GREEN, col1X, rowY);
        int bonus = dayManager.calculateDayBonus();
        drawStatRow("Bonus giornata", "+ $ " + bonus, GOLD, col2X, rowY);
        rowY -= rowH;

        drawStatRow("Piatti cucinati", String.valueOf(dayManager.dishesCooked), BLUE, col1X, rowY);
        int totalEarned = dayManager.coinsEarned + bonus;
        drawStatRow("TOTALE", "$ " + totalEarned, GOLD, col2X, rowY);
        rowY -= rowH;

        drawStatRow("Errori", String.valueOf(dayManager.mistakesCount),
                dayManager.mistakesCount > 3 ? RED : GREEN, col1X, rowY);
        rowY -= rowH;

        // Soddisfazione media
        float sat = dayManager.avgSatisfaction;
        String satLabel = sat >= 0.9f ? "Eccellente!" : sat >= 0.7f ? "Buona" : sat >= 0.5f ? "Discreta" : "Scarsa";
        Color satColor  = sat >= 0.9f ? GREEN : sat >= 0.7f ? BLUE : sat >= 0.5f ? GOLD : RED;
        drawStatRow("Soddisfazione media", satLabel, satColor, col1X, rowY);
        rowY -= rowH;

        // Badge giornata perfetta
        if (dayManager.customersLost == 0 && dayManager.customersServed > 0) {
            fillRect(col1X, rowY - 5, 250, 38, new Color(0.95f, 0.85f, 0.10f, 0.20f));
            strokeRect(col1X, rowY - 5, 250, 38, GOLD, 2f);
            assets.fontMedium.setColor(GOLD);
            drawText("★ GIORNATA PERFETTA!", col1X + 10, rowY + 25);
            assets.fontMedium.setColor(Color.WHITE);
        }

        // --- Monete totali ---
        assets.fontMedium.setColor(DARK);
        drawTextCentered("Monete totali: $ " + gameState.coins, cx, panY + 55);
        assets.fontMedium.setColor(Color.WHITE);

        // --- Pulsanti azione ---
        float btnW = 260f, btnH = 56f;
        float btnY = panY - 75f;

        if (drawButton("VAI AL NEGOZIO", panX + 30, btnY, btnW, btnH,
                BLUE, Color.WHITE)) {
            AudioManager.getInstance().playButton();
            nextDay();
            game.setScreen(new ShopScreen(game, gameState));
        }

        if (drawButton("PROSSIMO GIORNO", panX + panW - btnW - 30, btnY, btnW, btnH,
                RED, Color.WHITE)) {
            AudioManager.getInstance().playButton();
            nextDay();
            game.setScreen(new MissionScreen(game, gameState));
        }
    }

    private void drawStatRow(String label, String value, Color valueColor, float x, float y) {
        assets.fontSmall.setColor(new Color(0.3f, 0.3f, 0.3f, 1f));
        drawTextSmall(label, x, y + 22);
        assets.fontMedium.setColor(valueColor);
        drawText(value, x, y);
        assets.fontMedium.setColor(Color.WHITE);
    }

    private void nextDay() {
        gameState.currentDay++;
        gameState.totalServed += dayManager.customersServed;
        gameState.save();
    }
}
