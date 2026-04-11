package com.sushidays.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.sushidays.SushiDaysGame;
import com.sushidays.entities.Customer;
import com.sushidays.entities.Customer.CustomerState;
import com.sushidays.entities.Dish;
import com.sushidays.entities.Recipe;
import com.sushidays.systems.*;
import com.sushidays.utils.Constants;
import com.sushidays.utils.GameState;

import java.util.List;

public class GameScreen extends BaseScreen {

    // --- Sistemi ---
    private final DayManager      dayManager;
    private final CustomerManager customerManager;
    private final CookingSystem   cookingSystem;
    private final InventorySystem inventory;

    // --- Ricette disponibili oggi ---
    private final List<Recipe> availableRecipes;

    // --- Layout costanti ---
    private static final float COOK_AREA_X  = 180f;
    private static final float COOK_AREA_Y  = 80f;
    private static final float COOK_AREA_W  = 920f;
    private static final float COOK_AREA_H  = 460f;
    private static final float HUD_H        = 60f;
    private static final float QUEUE_Y      = 195f;

    // --- Stato UI ---
    private Recipe  selectedRecipe   = null;   // ricetta selezionata dal menu
    private float   feedbackTimer    = 0f;
    private String  feedbackText     = "";
    private Color   feedbackColor    = Color.WHITE;

    // --- Drag tracking per mini-giochi ---
    private boolean isDragging  = false;
    private float   touchStartX = 0, touchStartY = 0;

    // Colori
    private static final Color BG_COOK   = new Color(0.92f, 0.88f, 0.82f, 1f);
    private static final Color BG_HUD    = new Color(0.20f, 0.15f, 0.10f, 0.92f);
    private static final Color RED       = new Color(0.85f, 0.20f, 0.15f, 1f);
    private static final Color BLUE      = new Color(0.20f, 0.50f, 0.80f, 1f);
    private static final Color GREEN     = new Color(0.20f, 0.70f, 0.30f, 1f);
    private static final Color YELLOW    = new Color(0.95f, 0.78f, 0.10f, 1f);
    private static final Color DARKGRAY  = new Color(0.20f, 0.20f, 0.20f, 1f);
    private static final Color PANEL_BG  = new Color(0.85f, 0.80f, 0.74f, 1f);

    public GameScreen(SushiDaysGame game, GameState gameState) {
        super(game, gameState);

        inventory        = new InventorySystem();
        inventory.dailyRestock(gameState.currentDay);
        availableRecipes = RecipeRegistry.getAvailableRecipes(gameState.currentDay);

        dayManager       = new DayManager(gameState);
        customerManager  = new CustomerManager(gameState, dayManager, availableRecipes);
        cookingSystem    = new CookingSystem(gameState, dayManager, inventory);

        dayManager.startService();
    }

    // ---------------------------------------------------------------
    // Render
    // ---------------------------------------------------------------

    @Override
    public void render(float delta) {
        handleInput();
        update(delta);

        viewport.apply();

        float W = Constants.WORLD_WIDTH;
        float H = Constants.WORLD_HEIGHT;

        // ---- Sfondo generale ----
        fillRect(0, 0, W, H, new Color(0.96f, 0.93f, 0.88f, 1f));

        // ---- Area cucina ----
        fillRect(COOK_AREA_X, COOK_AREA_Y, COOK_AREA_W, COOK_AREA_H, BG_COOK);
        strokeRect(COOK_AREA_X, COOK_AREA_Y, COOK_AREA_W, COOK_AREA_H, new Color(0.7f, 0.65f, 0.58f, 1f), 2f);

        // ---- HUD superiore ----
        drawHUD(W, H);

        // ---- Area clienti ----
        drawCustomersArea(W, H);

        // ---- Slot di cottura ----
        drawCookingSlots();

        // ---- Menu ricette ----
        drawRecipeMenu(W);

        // ---- Feedback testuale ----
        if (feedbackTimer > 0) {
            assets.fontMedium.setColor(feedbackColor);
            drawTextCentered(feedbackText, W / 2f, H / 2f + 40);
            assets.fontMedium.setColor(Color.WHITE);
        }

        // ---- Transizione fine giornata ----
        if (dayManager.isDayOver() && customerManager.allGone()) {
            drawEndDayTransition(W, H);
        }
    }

    // ---------------------------------------------------------------
    // HUD
    // ---------------------------------------------------------------

    private void drawHUD(float W, float H) {
        float hudY = H - HUD_H;
        fillRect(0, hudY, W, HUD_H, BG_HUD);

        // Timer
        float remaining = dayManager.getTimeRemaining();
        String timeStr = String.format("%d:%02d", (int) remaining / 60, (int) remaining % 60);
        assets.fontMedium.setColor(dayManager.isRushHour() ? YELLOW : Color.WHITE);
        drawText((dayManager.isRushHour() ? "⚡ RUSH HOUR  " : "") + timeStr,
                W / 2f - 80, H - 12);
        assets.fontMedium.setColor(Color.WHITE);

        // Monete
        assets.fontMedium.setColor(YELLOW);
        drawText("$ " + gameState.coins, 20, H - 12);
        assets.fontMedium.setColor(Color.WHITE);

        // Progresso giornata
        drawProgressBar(0, hudY, W, 5,
                dayManager.getProgressFraction(),
                DARKGRAY, dayManager.isRushHour() ? YELLOW : RED);

        // Pulsante pausa
        if (drawButton("II", W - 70, H - HUD_H + 5, 55, 45,
                new Color(0.40f, 0.40f, 0.40f, 0.9f), Color.WHITE)) {
            game.setScreen(new PauseScreen(game, gameState, this));
        }
    }

    // ---------------------------------------------------------------
    // Clienti
    // ---------------------------------------------------------------

    private void drawCustomersArea(float W, float H) {
        // Sfondo barra clienti
        fillRect(0, H - HUD_H - 105, W, 100, new Color(0.88f, 0.84f, 0.78f, 1f));
        strokeRect(0, H - HUD_H - 105, W, 100, new Color(0.75f, 0.70f, 0.63f, 1f), 1f);

        for (Customer c : customerManager.getCustomers()) {
            drawCustomer(c, H);
        }
    }

    private void drawCustomer(Customer c, float H) {
        float baseY = H - HUD_H - 110;
        float cx = c.x;

        // Corpo cliente (rettangolo colorato)
        Color bodyColor = c.type.color.cpy();
        if (c.state == CustomerState.EATING) bodyColor.mul(1f, 1f, 1f, 0.7f);
        fillRect(cx - 30, baseY - 60, 60, 75, bodyColor);

        // Testa
        fillCircle(cx, baseY + 28, 22, bodyColor);
        strokeRect(cx - 30, baseY - 60, 60, 75, new Color(0f, 0f, 0f, 0.3f), 1.5f);

        // Badge tipo
        if (c.type != Customer.CustomerType.NORMAL) {
            assets.fontSmall.setColor(Color.WHITE);
            drawTextSmall(c.type.label, cx - 25, baseY - 62);
        }

        // Barra pazienza
        if (c.state == CustomerState.WAITING) {
            float pf = c.getPatienceFraction();
            Color pColor = pf > 0.5f ? GREEN : (pf > 0.25f ? YELLOW : RED);
            drawProgressBar(cx - 35, baseY - 75, 70, 8, pf,
                    new Color(0.3f, 0.3f, 0.3f, 0.8f), pColor);
        }

        // Bolla ordine
        if (c.state == CustomerState.WAITING && c.currentDesiredRecipe() != null) {
            drawOrderBubble(c, cx, baseY);
        }

        // Emoji stato
        String emoji = "";
        if (c.state == CustomerState.EATING)  emoji = ":D";
        if (c.state == CustomerState.LEAVING && c.satisfaction > 0.5f) emoji = "^_^";
        if (c.state == CustomerState.LEAVING && c.satisfaction <= 0.5f) emoji = ">:(";
        if (!emoji.isEmpty()) {
            assets.fontSmall.setColor(Color.WHITE);
            drawTextSmall(emoji, cx - 12, baseY + 45);
        }
    }

    private void drawOrderBubble(Customer c, float cx, float baseY) {
        Recipe desired = c.currentDesiredRecipe();
        if (desired == null) return;

        fillRect(cx - 55, baseY + 40, 110, 40, Color.WHITE);
        strokeRect(cx - 55, baseY + 40, 110, 40, desired.dishColor, 2f);

        assets.fontSmall.setColor(desired.dishColor);
        assets.layout.setText(assets.fontSmall, desired.displayName);
        float tx = cx - assets.layout.width / 2f;
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        assets.fontSmall.draw(batch, desired.displayName, tx, baseY + 70);
        batch.end();
        assets.fontSmall.setColor(Color.WHITE);
    }

    // ---------------------------------------------------------------
    // Slot di cottura
    // ---------------------------------------------------------------

    private void drawCookingSlots() {
        int slots = gameState.hasExtraCooking ? 2 : 1;
        float slotW = gameState.hasExtraCooking ? 420f : 500f;
        float slotH = 260f;
        float gap   = 40f;
        float totalW = slots * slotW + (slots - 1) * gap;
        float startX = COOK_AREA_X + (COOK_AREA_W - totalW) / 2f;
        float slotY  = COOK_AREA_Y + (COOK_AREA_H - slotH) / 2f;

        for (int i = 0; i < slots; i++) {
            float sx = startX + i * (slotW + gap);
            drawCookingSlot(i, sx, slotY, slotW, slotH);
        }
    }

    private void drawCookingSlot(int slotIdx, float x, float y, float w, float h) {
        CookingSystem.CookingState state = cookingSystem.getSlotState(slotIdx);
        Dish dish = cookingSystem.getSlotDish(slotIdx);

        // Sfondo slot
        Color slotBg = new Color(0.82f, 0.78f, 0.70f, 1f);
        if (state == CookingSystem.CookingState.STEP_ACTIVE)  slotBg = new Color(0.95f, 0.92f, 0.82f, 1f);
        if (state == CookingSystem.CookingState.DISH_DONE)    slotBg = new Color(0.80f, 0.95f, 0.80f, 1f);
        fillRect(x, y, w, h, slotBg);
        strokeRect(x, y, w, h, new Color(0.60f, 0.55f, 0.48f, 1f), 2f);

        if (dish == null || state == CookingSystem.CookingState.IDLE) {
            // Slot vuoto
            assets.fontSmall.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
            drawTextCentered(cookingSystem.getQueueSize() > 0 ? "Preparazione in coda..." : "Tocca una ricetta per iniziare",
                    x + w / 2f, y + h / 2f + 10);
            assets.fontSmall.setColor(Color.WHITE);
            return;
        }

        Recipe recipe = dish.recipe;
        float cx = x + w / 2f;

        // Nome piatto
        assets.fontMedium.setColor(recipe.dishColor);
        drawTextCentered(recipe.displayName, cx, y + h - 18);
        assets.fontMedium.setColor(Color.WHITE);

        // Disegno piatto (rettangolo colorato placeholder)
        fillRect(cx - 55, y + h / 2f - 5, 110, 60, recipe.dishColor);
        strokeRect(cx - 55, y + h / 2f - 5, 110, 60, Color.WHITE, 1.5f);

        if (state == CookingSystem.CookingState.STEP_ACTIVE) {
            drawActiveStep(dish, slotIdx, x, y, w, h);
        } else if (state == CookingSystem.CookingState.STEP_RESULT) {
            drawStepResult(dish, x, y, w, h);
        } else if (state == CookingSystem.CookingState.DISH_DONE) {
            drawDishReady(dish, x, y, w, h);
        }
    }

    private void drawActiveStep(Dish dish, int slotIdx, float x, float y, float w, float h) {
        var step = dish.currentStep();
        if (step == null) return;

        // Istruzione
        assets.fontMedium.setColor(Color.BLACK);
        drawTextCentered(step.instruction, x + w / 2f, y + 55);
        assets.fontMedium.setColor(Color.WHITE);

        // Icona tipo step
        String icon = getStepIcon(step.type);
        assets.fontLarge.setColor(new Color(0.85f, 0.20f, 0.15f, 1f));
        drawTextCentered(icon, x + w / 2f, y + 95);
        assets.fontLarge.setColor(Color.WHITE);

        // Timer step
        float stepFraction = 1f - (cookingSystem.getStepTimer(slotIdx) / Constants.STEP_TIMEOUT);
        Color timerColor = stepFraction > 0.5f ? GREEN : (stepFraction > 0.25f ? YELLOW : RED);
        drawProgressBar(x + 20, y + 18, w - 40, 10, stepFraction,
                new Color(0.3f, 0.3f, 0.3f, 0.8f), timerColor);

        // Progresso step
        int done = dish.currentStepIndex;
        int total = dish.activeSteps.size();
        assets.fontSmall.setColor(DARKGRAY);
        drawText("Step " + (done + 1) + "/" + total, x + 10, y + h - 22);
        assets.fontSmall.setColor(Color.WHITE);
    }

    private void drawStepResult(Dish dish, float x, float y, float w, float h) {
        if (dish.currentStepIndex > 0 && dish.currentStepIndex <= dish.activeSteps.size()) {
            var lastStep = dish.activeSteps.get(dish.currentStepIndex - 1);
            int score = lastStep.score;
            String label = score >= 85 ? "ECCELLENTE!" : score >= 65 ? "BUONO" : score >= 40 ? "PASSABILE" : "SCARSO";
            Color col    = score >= 85 ? GREEN : score >= 65 ? BLUE : score >= 40 ? YELLOW : RED;
            assets.fontMedium.setColor(col);
            drawTextCentered(label + " (" + score + ")", x + w / 2f, y + 55);
            assets.fontMedium.setColor(Color.WHITE);
        }
    }

    private void drawDishReady(Dish dish, float x, float y, float w, float h) {
        assets.fontMedium.setColor(GREEN);
        drawTextCentered("PRONTO!", x + w / 2f, y + 55);
        assets.fontMedium.setColor(Color.WHITE);

        assets.fontSmall.setColor(GREEN);
        drawTextCentered("Tocca un cliente per consegnare", x + w / 2f, y + 30);
        assets.fontSmall.setColor(Color.WHITE);

        // Qualità
        if (dish.quality != null) {
            assets.fontSmall.setColor(dish.quality.color);
            drawTextCentered(dish.quality.label, x + w / 2f, y + 15);
            assets.fontSmall.setColor(Color.WHITE);
        }
    }

    private String getStepIcon(com.sushidays.entities.CookingStep.StepType type) {
        switch (type) {
            case SLICE: return "///";
            case ROLL:  return "O";
            case PRESS: return "v";
            case SHAKE: return "~~~";
            case POUR:  return "|";
            case WAIT:  return "( )";
            default:    return "?";
        }
    }

    // ---------------------------------------------------------------
    // Menu ricette
    // ---------------------------------------------------------------

    private void drawRecipeMenu(float W) {
        float menuX = 5f, menuY = COOK_AREA_Y, menuW = 170f, menuH = COOK_AREA_H;
        fillRect(menuX, menuY, menuW, menuH, PANEL_BG);
        strokeRect(menuX, menuY, menuW, menuH, new Color(0.7f, 0.65f, 0.58f, 1f), 1.5f);

        assets.fontSmall.setColor(DARKGRAY);
        drawText("MENU", menuX + 50, menuY + menuH - 12);
        assets.fontSmall.setColor(Color.WHITE);

        float itemH = 48f, startY = menuY + menuH - 55;
        for (Recipe r : availableRecipes) {
            boolean canCook = cookingSystem.canCook(r);
            boolean selected = r == selectedRecipe;

            Color bg = selected ? r.dishColor.cpy().mul(1.2f, 1.2f, 1.2f, 1f)
                    : (canCook ? r.dishColor.cpy().mul(0.85f, 0.85f, 0.85f, 1f)
                               : new Color(0.65f, 0.65f, 0.65f, 1f));

            fillRect(menuX + 5, startY, menuW - 10, itemH - 4, bg);
            if (!canCook) strokeRect(menuX + 5, startY, menuW - 10, itemH - 4,
                    new Color(0.4f, 0.4f, 0.4f, 0.5f), 1f);

            assets.fontSmall.setColor(Color.WHITE);
            drawTextSmall(r.displayName, menuX + 10, startY + itemH - 8);
            assets.fontSmall.setColor(YELLOW);
            drawTextSmall("$" + r.basePrice, menuX + 10, startY + itemH - 26);
            if (!canCook) {
                assets.fontSmall.setColor(new Color(0.9f, 0.3f, 0.3f, 1f));
                drawTextSmall("No ingr.", menuX + 10, startY + 6);
            }
            assets.fontSmall.setColor(Color.WHITE);

            // Click su ricetta
            if (isTouched(menuX + 5, startY, menuW - 10, itemH - 4) && canCook) {
                selectedRecipe = r;
                cookingSystem.enqueueDish(r);
            }
            startY -= itemH;
            if (startY < menuY) break;
        }

        // Coda
        assets.fontSmall.setColor(DARKGRAY);
        drawText("Coda: " + cookingSystem.getQueueSize(), menuX + 10, menuY + 18);
        assets.fontSmall.setColor(Color.WHITE);

        // Area destra menu per info inventory
        float invX = W - 175f;
        fillRect(invX, COOK_AREA_Y, 170f, COOK_AREA_H, PANEL_BG);
        strokeRect(invX, COOK_AREA_Y, 170f, COOK_AREA_H, new Color(0.7f, 0.65f, 0.58f, 1f), 1.5f);
        assets.fontSmall.setColor(DARKGRAY);
        drawText("SCORTE", invX + 35, COOK_AREA_Y + COOK_AREA_H - 12);
        assets.fontSmall.setColor(Color.WHITE);

        float iy = COOK_AREA_Y + COOK_AREA_H - 40;
        for (com.sushidays.entities.Ingredient.Type t : com.sushidays.entities.Ingredient.Type.values()) {
            var ing = inventory.get(t);
            if (!ing.unlocked) continue;
            Color c = ing.quantity > 5 ? GREEN : (ing.quantity > 0 ? YELLOW : RED);
            assets.fontSmall.setColor(c);
            drawTextSmall(ing.type.displayName + ": " + ing.quantity, invX + 8, iy);
            assets.fontSmall.setColor(Color.WHITE);
            iy -= 22;
            if (iy < COOK_AREA_Y + 10) break;
        }
    }

    // ---------------------------------------------------------------
    // Fine giornata
    // ---------------------------------------------------------------

    private float endDayTimer = 0f;

    private void drawEndDayTransition(float W, float H) {
        endDayTimer += Gdx.graphics.getDeltaTime();
        float alpha = Math.min(1f, endDayTimer / 1.5f);
        fillRect(0, 0, W, H, new Color(0f, 0f, 0f, alpha * 0.6f));

        if (alpha >= 1f) {
            // Vai a DayEndScreen — aggiungiamo solo il bonus finale
            // (i guadagni per ogni piatto sono già stati aggiunti in tempo reale)
            int bonus = dayManager.calculateDayBonus();
            if (bonus > 0) gameState.addCoins(bonus);
            game.setScreen(new DayEndScreen(game, gameState, dayManager));
        } else {
            assets.fontLarge.setColor(new Color(1f, 1f, 1f, alpha));
            drawTextCenteredLarge("Giornata terminata!", W / 2f, H / 2f + 20);
            assets.fontLarge.setColor(Color.WHITE);
        }
    }

    // ---------------------------------------------------------------
    // Update & Input
    // ---------------------------------------------------------------

    private void update(float delta) {
        dayManager.update(delta);
        customerManager.update(delta);
        cookingSystem.update(delta);

        if (feedbackTimer > 0) feedbackTimer -= delta;

        // Consegna automatica se piatto pronto e cliente aspetta
        if (cookingSystem.hasDishReady()) {
            tryAutoDeliver();
        }

        // Back button / Escape → pausa
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            game.setScreen(new PauseScreen(game, gameState, this));
        }
    }

    private void handleInput() {
        // Input gestito interamente dall'InputAdapter registrato in show()
    }

    private void tryAutoDeliver() {
        // Cerca il primo cliente che aspetta esattamente il piatto pronto
        for (Customer c : customerManager.getCustomers()) {
            if (!c.isWaiting()) continue;
            Dish ready = cookingSystem.takeFirstReady();
            if (ready != null && c.currentDesiredRecipe() != null
                    && c.currentDesiredRecipe().id.equals(ready.recipe.id)) {
                c.receiveDish(ready);
                dayManager.recordServed(ready.calculateEarnings(), c.satisfaction);
                gameState.addCoins(ready.calculateEarnings());
                showFeedback("+" + ready.calculateEarnings() + " $  " + ready.quality.label,
                        ready.quality.color);
                AudioManager.getInstance().playCoin();
                return;
            }
            // Rimetti nella lista se non è il piatto giusto
            // (non re-enqueuiamo, viene rimesso dallo slot)
        }
    }

    private void deliverToCustomer(Customer c) {
        Dish dish = cookingSystem.takeFirstReady();
        if (dish == null) return;
        c.receiveDish(dish);
        dayManager.recordServed(dish.calculateEarnings(), c.satisfaction);
        gameState.addCoins(dish.calculateEarnings() + c.calculateTip());
        showFeedback("+" + (dish.calculateEarnings() + c.calculateTip()) + " $  " + dish.quality.label,
                dish.quality.color);
        AudioManager.getInstance().playCoin();
    }

    private void showFeedback(String text, Color color) {
        feedbackText  = text;
        feedbackColor = color;
        feedbackTimer = 1.8f;
    }

    // Override necessario per il drag input durante mini-giochi
    @Override
    public void show() {
        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 v = viewport.unproject(new Vector3(screenX, screenY, 0));
                cookingSystem.onTouchDown(v.x, v.y, cookingSystem.getActiveSlot());
                touchStartX = v.x; touchStartY = v.y; isDragging = true;
                return true;
            }
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                Vector3 v = viewport.unproject(new Vector3(screenX, screenY, 0));
                cookingSystem.onTouchDragged(v.x, v.y, cookingSystem.getActiveSlot());
                return true;
            }
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                Vector3 v = viewport.unproject(new Vector3(screenX, screenY, 0));
                cookingSystem.onTouchUp(v.x, v.y, cookingSystem.getActiveSlot());
                isDragging = false;
                // Controlla click su cliente per consegna
                if (cookingSystem.hasDishReady()) {
                    Customer c = customerManager.getCustomerAt(v.x, v.y);
                    if (c != null && c.isWaiting()) deliverToCustomer(c);
                }
                return true;
            }
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE ||
                    keycode == com.badlogic.gdx.Input.Keys.BACK) {
                    game.setScreen(new PauseScreen(game, gameState, GameScreen.this));
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }
}
