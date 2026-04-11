package com.sushidays.screens;

import com.badlogic.gdx.graphics.Color;
import com.sushidays.SushiDaysGame;
import com.sushidays.entities.Upgrade;
import com.sushidays.entities.Upgrade.UpgradeType;
import com.sushidays.systems.AudioManager;
import com.sushidays.systems.UpgradeRegistry;
import com.sushidays.utils.Constants;
import com.sushidays.utils.GameState;

import java.util.List;

public class ShopScreen extends BaseScreen {

    private enum Tab { FUNCTIONAL, COSMETIC }
    private Tab currentTab = Tab.FUNCTIONAL;

    private String feedbackMsg   = "";
    private Color  feedbackColor = Color.WHITE;
    private float  feedbackTimer = 0f;

    private static final Color RED   = new Color(0.85f, 0.20f, 0.15f, 1f);
    private static final Color BLUE  = new Color(0.20f, 0.50f, 0.80f, 1f);
    private static final Color GREEN = new Color(0.20f, 0.70f, 0.30f, 1f);
    private static final Color GOLD  = new Color(0.95f, 0.78f, 0.10f, 1f);
    private static final Color GRAY  = new Color(0.55f, 0.55f, 0.55f, 1f);
    private static final Color BG    = new Color(0.96f, 0.93f, 0.88f, 1f);

    public ShopScreen(SushiDaysGame game, GameState gameState) {
        super(game, gameState);
    }

    @Override
    public void show() {
        AudioManager.getInstance().playMenuMusic();
    }

    @Override
    public void render(float delta) {
        feedbackTimer -= delta;
        viewport.apply();

        float W = Constants.WORLD_WIDTH;
        float H = Constants.WORLD_HEIGHT;
        float cx = W / 2f;

        fillRect(0, 0, W, H, BG);

        // Header
        fillRect(0, H - 75, W, 75, RED);
        assets.fontLarge.setColor(Color.WHITE);
        drawTextCenteredLarge("NEGOZIO", cx, H - 20);
        assets.fontLarge.setColor(Color.WHITE);

        // Monete
        assets.fontMedium.setColor(GOLD);
        drawText("$ " + gameState.coins, 20, H - 20);
        assets.fontMedium.setColor(Color.WHITE);

        // Tab buttons
        float tabW = 220f, tabH = 46f, tabY = H - 75 - tabH;
        boolean funcActive = currentTab == Tab.FUNCTIONAL;
        if (drawButton("UPGRADE CUCINA", cx - tabW - 10, tabY, tabW, tabH,
                funcActive ? RED : GRAY, Color.WHITE)) {
            currentTab = Tab.FUNCTIONAL;
            AudioManager.getInstance().playButton();
        }
        if (drawButton("COSMETICI", cx + 10, tabY, tabW, tabH,
                !funcActive ? RED : GRAY, Color.WHITE)) {
            currentTab = Tab.COSMETIC;
            AudioManager.getInstance().playButton();
        }

        // Lista upgrade
        List<Upgrade> items = currentTab == Tab.FUNCTIONAL
                ? UpgradeRegistry.getFunctional()
                : UpgradeRegistry.getCosmetics();
        drawItemGrid(items, W, H, tabY);

        // Feedback acquisto
        if (feedbackTimer > 0) {
            assets.fontMedium.setColor(feedbackColor);
            drawTextCentered(feedbackMsg, cx, 100);
            assets.fontMedium.setColor(Color.WHITE);
        }

        // Pulsante torna indietro
        if (drawButton("INDIETRO", 30, 20, 200, 50, GRAY, Color.WHITE)) {
            AudioManager.getInstance().playButton();
            game.setScreen(new MainMenuScreen(game, gameState));
        }
    }

    private void drawItemGrid(List<Upgrade> items, float W, float H, float tabY) {
        float cols = 3f;
        float margin = 30f;
        float gap    = 20f;
        float cardW  = (W - margin * 2 - gap * (cols - 1)) / cols;
        float cardH  = 160f;
        float startX = margin;
        float startY = tabY - cardH - gap;

        for (int i = 0; i < items.size(); i++) {
            Upgrade u = items.get(i);
            int col  = i % (int) cols;
            int row  = i / (int) cols;
            float cx = startX + col * (cardW + gap);
            float cy = startY - row * (cardH + gap);
            if (cy < 90) break; // non scende oltre

            drawUpgradeCard(u, cx, cy, cardW, cardH);
        }
    }

    private void drawUpgradeCard(Upgrade u, float x, float y, float w, float h) {
        boolean owned     = gameState.hasUpgrade(u.id);
        boolean locked    = gameState.currentDay < u.unlockDay;
        boolean canAfford = gameState.coins >= u.cost;

        // Colore card
        Color cardBg;
        if (owned)   cardBg = new Color(0.85f, 0.95f, 0.85f, 1f);
        else if (locked) cardBg = new Color(0.88f, 0.88f, 0.88f, 1f);
        else         cardBg = Color.WHITE;

        fillRect(x, y, w, h, cardBg);
        Color borderCol = owned ? GREEN : (locked ? GRAY : (canAfford ? BLUE : RED));
        strokeRect(x, y, w, h, borderCol, 2f);

        // Icona tipo (rettangolo colorato placeholder)
        Color iconColor = u.type == UpgradeType.FUNCTIONAL ? BLUE : new Color(0.90f, 0.50f, 0.80f, 1f);
        fillRect(x + 10, y + h - 44, 36, 30, iconColor);

        // Nome
        assets.fontMedium.setColor(owned ? GREEN : (locked ? GRAY : Color.BLACK));
        drawText(u.displayName, x + 55, y + h - 20);
        assets.fontMedium.setColor(Color.WHITE);

        // Descrizione
        assets.fontSmall.setColor(new Color(0.4f, 0.4f, 0.4f, 1f));
        drawTextSmall(u.description, x + 10, y + h - 55);
        assets.fontSmall.setColor(Color.WHITE);

        if (locked) {
            assets.fontSmall.setColor(GRAY);
            drawTextSmall("Sblocca al giorno " + u.unlockDay, x + 10, y + 40);
            assets.fontSmall.setColor(Color.WHITE);
        }

        // Pulsante
        if (owned) {
            fillRect(x + 10, y + 12, w - 20, 32, new Color(0.80f, 0.95f, 0.80f, 1f));
            assets.fontSmall.setColor(GREEN);
            drawTextCentered("✓ POSSEDUTO", x + w / 2f, y + 34);
            assets.fontSmall.setColor(Color.WHITE);
        } else if (locked) {
            fillRect(x + 10, y + 12, w - 20, 32, new Color(0.85f, 0.85f, 0.85f, 1f));
            assets.fontSmall.setColor(GRAY);
            drawTextCentered("BLOCCATO", x + w / 2f, y + 34);
            assets.fontSmall.setColor(Color.WHITE);
        } else {
            Color btnColor = canAfford ? BLUE : new Color(0.65f, 0.65f, 0.65f, 1f);
            if (drawButton("ACQUISTA  $ " + u.cost, x + 10, y + 12, w - 20, 32, btnColor, Color.WHITE)) {
                if (canAfford) {
                    tryPurchase(u);
                } else {
                    showFeedback("Monete insufficienti!", RED);
                }
            }
        }
    }

    private void tryPurchase(Upgrade u) {
        if (gameState.spendCoins(u.cost)) {
            if (u.type == UpgradeType.FUNCTIONAL) {
                gameState.purchaseUpgrade(u);
            } else {
                gameState.purchaseCosmetic(u.id);
                gameState.activeCosmetic = u.id;
            }
            gameState.save();
            AudioManager.getInstance().playCoin();
            showFeedback("Acquistato: " + u.displayName + "!", GREEN);
        }
    }

    private void showFeedback(String msg, Color color) {
        feedbackMsg   = msg;
        feedbackColor = color;
        feedbackTimer = 2f;
    }
}
