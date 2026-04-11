package com.sushidays.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.sushidays.entities.CookingStep;
import com.sushidays.entities.CookingStep.StepType;
import com.sushidays.entities.Dish;
import com.sushidays.entities.Recipe;
import com.sushidays.utils.Constants;
import com.sushidays.utils.GameState;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Gestisce la preparazione dei piatti tramite mini-giochi.
 * Può gestire fino a 2 piatti in parallelo (con upgrade extra_cooking).
 */
public class CookingSystem {

    public enum CookingState {
        IDLE,          // nessun piatto in preparazione
        STEP_ACTIVE,   // step corrente in attesa di input
        STEP_RESULT,   // mostra il risultato dello step (breve pausa)
        DISH_DONE      // piatto completato, pronto per essere rimosso
    }

    // Slot di cottura (1 o 2 a seconda dell'upgrade)
    public static final int MAX_SLOTS = 2;

    private final GameState      state;
    private final DayManager     dayManager;
    private final InventorySystem inventory;

    // Slot 0 sempre attivo, slot 1 richiede upgrade extra_cooking
    private final Dish[]         slots       = new Dish[MAX_SLOTS];
    private final CookingState[] slotStates  = new CookingState[MAX_SLOTS];
    private final float[]        stepTimers  = new float[MAX_SLOTS];
    private final float[]        resultTimers= new float[MAX_SLOTS];

    // Coda di piatti da preparare
    private final Queue<Dish>    dishQueue   = new LinkedList<>();

    // Tracking input per i mini-giochi
    private final Vector2        lastTouch   = new Vector2();
    private float                holdTimer   = 0f;
    private float                shakeAccum  = 0f;
    private final List<Vector2>  swipePath   = new ArrayList<>();
    private float                rotAngle    = 0f;
    private float                prevTouchX  = 0, prevTouchY = 0;

    // Slot attivo per l'input (quello su cui sta lavorando l'utente)
    private int activeSlot = 0;

    public CookingSystem(GameState state, DayManager dayManager, InventorySystem inventory) {
        this.state     = state;
        this.dayManager = dayManager;
        this.inventory  = inventory;
        for (int i = 0; i < MAX_SLOTS; i++) slotStates[i] = CookingState.IDLE;
    }

    // ---------------------------------------------------------------
    // Update
    // ---------------------------------------------------------------

    public void update(float delta) {
        for (int i = 0; i < MAX_SLOTS; i++) {
            updateSlot(i, delta);
        }
        // Alimenta la coda nei slot liberi
        fillSlotsFromQueue();
    }

    private void updateSlot(int slot, float delta) {
        if (slotStates[slot] == CookingState.IDLE) return;
        Dish dish = slots[slot];
        if (dish == null) return;

        switch (slotStates[slot]) {
            case STEP_ACTIVE:
                stepTimers[slot] += delta;
                // Timeout step
                if (stepTimers[slot] >= Constants.STEP_TIMEOUT) {
                    completeStep(slot, 0); // score 0 = timeout
                    dayManager.recordMistake();
                    AudioManager.getInstance().playError();
                }
                // Input hold per PRESS e WAIT
                updateHoldInput(slot, delta, dish.currentStep());
                break;

            case STEP_RESULT:
                resultTimers[slot] += delta;
                if (resultTimers[slot] >= 0.7f) {
                    resultTimers[slot] = 0f;
                    advanceToNextStep(slot);
                }
                break;

            case DISH_DONE:
                // Rimane DISH_DONE finché non viene rimosso dalla GameScreen
                break;

            default:
                break;
        }
    }

    private void updateHoldInput(int slot, float delta, CookingStep step) {
        if (step == null) return;
        if ((step.type == StepType.PRESS || step.type == StepType.WAIT)
                && Gdx.input.isTouched()) {
            holdTimer += delta;
        }
    }

    // ---------------------------------------------------------------
    // Gestione slot e coda
    // ---------------------------------------------------------------

    public void enqueueDish(Recipe recipe) {
        if (!canCook(recipe)) return;
        dishQueue.add(new Dish(recipe));
        fillSlotsFromQueue();
    }

    private void fillSlotsFromQueue() {
        if (dishQueue.isEmpty()) return;
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (!isSlotAvailable(i)) continue;
            Dish d = dishQueue.poll();
            if (d == null) return;
            slots[i]      = d;
            slotStates[i] = CookingState.STEP_ACTIVE;
            stepTimers[i] = 0f;
            d.state       = Dish.State.COOKING;
            resetInputTrackers();
        }
    }

    private boolean isSlotAvailable(int slot) {
        if (slot == 1 && !state.hasExtraCooking) return false;
        return slotStates[slot] == CookingState.IDLE;
    }

    public boolean canCook(Recipe recipe) {
        for (CookingStep step : recipe.steps) {
            if (!inventory.hasIngredient(step.ingredient)) return false;
        }
        return true;
    }

    /** Rimuove il piatto completato dallo slot e lo ritorna (per consegnarlo al cliente). */
    public Dish takeDoneFromSlot(int slot) {
        if (slotStates[slot] != CookingState.DISH_DONE) return null;
        Dish d       = slots[slot];
        slots[slot]  = null;
        slotStates[slot] = CookingState.IDLE;
        fillSlotsFromQueue();
        return d;
    }

    /** Restituisce il primo piatto pronto tra tutti gli slot. */
    public Dish takeFirstReady() {
        for (int i = 0; i < MAX_SLOTS; i++) {
            Dish d = takeDoneFromSlot(i);
            if (d != null) return d;
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Input mini-giochi
    // ---------------------------------------------------------------

    /** Chiamato dalla GameScreen ad ogni touchDown. */
    public void onTouchDown(float x, float y, int slot) {
        if (slotStates[slot] != CookingState.STEP_ACTIVE) return;
        lastTouch.set(x, y);
        prevTouchX = x; prevTouchY = y;
        swipePath.clear();
        swipePath.add(new Vector2(x, y));
        holdTimer  = 0f;
        shakeAccum = 0f;
        rotAngle   = 0f;
    }

    /** Chiamato ad ogni touchDragged. */
    public void onTouchDragged(float x, float y, int slot) {
        if (slotStates[slot] != CookingState.STEP_ACTIVE) return;
        CookingStep step = slots[slot] != null ? slots[slot].currentStep() : null;
        if (step == null) return;

        swipePath.add(new Vector2(x, y));

        // SHAKE: accumuliamo variazioni
        if (step.type == StepType.SHAKE) {
            shakeAccum += Math.abs(x - prevTouchX) + Math.abs(y - prevTouchY);
        }

        // ROLL: stimiamo angolo percorso
        if (step.type == StepType.ROLL) {
            float cx = 640f, cy = 200f; // centro area cottura
            float angle = (float) Math.toDegrees(Math.atan2(y - cy, x - cx));
            rotAngle += 3f; // approssimazione semplice
        }

        prevTouchX = x; prevTouchY = y;
    }

    /** Chiamato ad ogni touchUp — valuta lo step corrente. */
    public void onTouchUp(float x, float y, int slot) {
        if (slotStates[slot] != CookingState.STEP_ACTIVE) return;
        CookingStep step = slots[slot] != null ? slots[slot].currentStep() : null;
        if (step == null) return;

        int score = evaluateStep(step, x, y);
        completeStep(slot, score);

        if (score >= Constants.EXCELLENT_THRESHOLD) {
            AudioManager.getInstance().playSuccess();
        } else if (score < Constants.PASSABLE_THRESHOLD) {
            AudioManager.getInstance().playError();
            dayManager.recordMistake();
        }
    }

    private int evaluateStep(CookingStep step, float upX, float upY) {
        float timeUsed = stepTimers[activeSlot];
        float timeFraction = 1f - (timeUsed / Constants.STEP_TIMEOUT);

        switch (step.type) {
            case SLICE:
                return evaluateSlice(upX, upY, timeFraction);
            case ROLL:
                return evaluateRoll(timeFraction);
            case PRESS:
                return evaluatePress(timeFraction);
            case SHAKE:
                return evaluateShake(timeFraction);
            case POUR:
                return evaluatePour(upX, upY, timeFraction);
            case WAIT:
                return evaluateWait(timeFraction);
            default:
                return 70;
        }
    }

    private int evaluateSlice(float upX, float upY, float timeFraction) {
        // Valuta linearità dello swipe
        if (swipePath.size() < 2) return 20;
        Vector2 start = swipePath.get(0);
        float totalDev = 0f;
        float dx = upX - start.x;
        float dy = upY - start.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 80f) return 30; // swipe troppo corto

        // Deviazione media dalla linea retta
        for (Vector2 p : swipePath) {
            float t = ((p.x - start.x) * dx + (p.y - start.y) * dy) / (len * len);
            float projX = start.x + t * dx;
            float projY = start.y + t * dy;
            totalDev += Math.sqrt((p.x - projX) * (p.x - projX) + (p.y - projY) * (p.y - projY));
        }
        float avgDev  = totalDev / swipePath.size();
        float accuracy = Math.max(0f, 1f - avgDev / 30f);

        // Bonus velocità upgrade
        float speedBonus = 1f + state.sliceSpeedBonus;
        return Math.min(100, Math.round(accuracy * 70f * speedBonus + timeFraction * 30f));
    }

    private int evaluateRoll(float timeFraction) {
        int angleScore = Math.min(100, Math.round(rotAngle / 360f * 80f));
        return Math.round(angleScore * 0.7f + timeFraction * 30f);
    }

    private int evaluatePress(float timeFraction) {
        // Ottimale: tenuto per 1.5-3.5 secondi
        float optimal = 2.5f;
        float diff = Math.abs(holdTimer - optimal);
        float score = Math.max(0f, 1f - diff / optimal);
        return Math.round(score * 75f + timeFraction * 25f);
    }

    private int evaluateShake(float timeFraction) {
        float target = 800f; // pixel totali di movimento
        float ratio  = Math.min(1f, shakeAccum / target);
        return Math.round(ratio * 75f + timeFraction * 25f);
    }

    private int evaluatePour(float upX, float upY, float timeFraction) {
        // Valuta velocità verticale del drag
        if (swipePath.size() < 2) return 40;
        Vector2 start = swipePath.get(0);
        float vertDist = Math.abs(upY - start.y);
        float score = Math.min(1f, vertDist / 200f);
        return Math.round(score * 75f + timeFraction * 25f);
    }

    private int evaluateWait(float timeFraction) {
        // Ottimale: tap a metà del timer (50% ± 15%)
        float ideal = 0.50f;
        float used  = 1f - timeFraction;
        float diff  = Math.abs(used - ideal);
        float score = Math.max(0f, 1f - diff / 0.25f);
        return Math.round(score * 100f);
    }

    // ---------------------------------------------------------------
    // Avanzamento step
    // ---------------------------------------------------------------

    private void completeStep(int slot, int score) {
        Dish dish = slots[slot];
        if (dish == null) return;
        CookingStep step = dish.currentStep();
        if (step == null) return;

        // Consuma ingrediente
        inventory.consume(step.ingredient, 1);
        step.complete(score);

        slotStates[slot] = CookingState.STEP_RESULT;
        resultTimers[slot] = 0f;
        dayManager.recordDish();
    }

    private void advanceToNextStep(int slot) {
        Dish dish = slots[slot];
        if (dish == null) return;

        if (dish.isLastStep()) {
            dish.finalize(true);
            slotStates[slot] = CookingState.DISH_DONE;
            AudioManager.getInstance().playCoin();
        } else {
            dish.advanceStep();
            slotStates[slot] = CookingState.STEP_ACTIVE;
            stepTimers[slot] = 0f;
            resetInputTrackers();
        }
    }

    private void resetInputTrackers() {
        holdTimer  = 0f;
        shakeAccum = 0f;
        rotAngle   = 0f;
        swipePath.clear();
    }

    // ---------------------------------------------------------------
    // Query
    // ---------------------------------------------------------------

    public Dish getSlotDish(int slot)         { return slots[slot];       }
    public CookingState getSlotState(int slot) { return slotStates[slot]; }
    public float getStepTimer(int slot)        { return stepTimers[slot]; }
    public int getQueueSize()                  { return dishQueue.size(); }
    public boolean isSlotActive(int slot)      { return slotStates[slot] != CookingState.IDLE; }
    public void setActiveSlot(int slot)        { activeSlot = slot; }
    public int  getActiveSlot()                { return activeSlot; }

    public boolean hasDishReady() {
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (slotStates[i] == CookingState.DISH_DONE) return true;
        }
        return false;
    }
}
