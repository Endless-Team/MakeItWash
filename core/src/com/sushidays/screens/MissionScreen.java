package com.sushidays.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.sushidays.SushiDaysGame;
import com.sushidays.systems.AudioManager;
import com.sushidays.systems.InventorySystem;
import com.sushidays.systems.MissionSystem;
import com.sushidays.systems.MissionSystem.Mission;
import com.sushidays.utils.Constants;
import com.sushidays.utils.GameState;

public class MissionScreen extends BaseScreen {

    private final MissionSystem   missionSystem;
    private final InventorySystem inventory;

    private float   stepFeedback   = 0f;
    private int     lastScore      = 0;
    private float   miniTimer      = 0f; // timer visivo per WAIT
    private boolean miniActive     = false;

    // Interazione
    private float dragStartX, dragStartY;
    private float dragAccum   = 0f;
    private float rotAccum    = 0f;
    private float holdTime    = 0f;
    private boolean holding   = false;

    private static final Color RED   = new Color(0.85f, 0.20f, 0.15f, 1f);
    private static final Color GREEN = new Color(0.20f, 0.70f, 0.30f, 1f);
    private static final Color BLUE  = new Color(0.20f, 0.50f, 0.80f, 1f);
    private static final Color GOLD  = new Color(0.95f, 0.78f, 0.10f, 1f);
    private static final Color BG    = new Color(0.96f, 0.93f, 0.88f, 1f);

    public MissionScreen(SushiDaysGame game, GameState gameState) {
        super(game, gameState);
        inventory     = new InventorySystem();
        missionSystem = new MissionSystem(gameState, inventory);
        missionSystem.generateDailyMissions();
    }

    @Override
    public void show() {
        AudioManager.getInstance().playMenuMusic();
        setupInput();
    }

    private void setupInput() {
        com.badlogic.gdx.Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean touchDown(int sx, int sy, int p, int b) {
                Vector3 v = viewport.unproject(new Vector3(sx, sy, 0));
                onDown(v.x, v.y);
                return true;
            }
            @Override
            public boolean touchDragged(int sx, int sy, int p) {
                Vector3 v = viewport.unproject(new Vector3(sx, sy, 0));
                onDrag(v.x, v.y);
                return true;
            }
            @Override
            public boolean touchUp(int sx, int sy, int p, int b) {
                Vector3 v = viewport.unproject(new Vector3(sx, sy, 0));
                onUp(v.x, v.y);
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        update(delta);
        viewport.apply();

        float W = Constants.WORLD_WIDTH;
        float H = Constants.WORLD_HEIGHT;
        float cx = W / 2f;

        fillRect(0, 0, W, H, BG);

        // Header
        fillRect(0, H - 75, W, 75, RED);
        assets.fontLarge.setColor(Color.WHITE);
        drawTextCenteredLarge("Preparazione — Giorno " + gameState.currentDay, cx, H - 15);
        assets.fontLarge.setColor(Color.WHITE);

        // Info monete
        assets.fontMedium.setColor(GOLD);
        drawText("$ " + gameState.coins, 20, H - 15);
        assets.fontMedium.setColor(Color.WHITE);

        // Lista missioni (sinistra)
        drawMissionList(W, H);

        // Area mini-gioco (centro/destra)
        drawMiniGame(W, H);

        // Pulsante INIZIA GIORNATA
        float bw = 320f, bh = 60f;
        if (drawButton("INIZIA LA GIORNATA!", W / 2f - bw / 2f, 20, bw, bh, RED, Color.WHITE)) {
            AudioManager.getInstance().playButton();
            com.badlogic.gdx.Gdx.input.setInputProcessor(null);
            game.setScreen(new GameScreen(game, gameState));
        }
    }

    // ---------------------------------------------------------------
    // Missioni
    // ---------------------------------------------------------------

    private void drawMissionList(float W, float H) {
        float listX = 30f, listY = 100f, listW = 340f;
        fillRect(listX, listY, listW, H - 190, new Color(0.99f, 0.97f, 0.94f, 1f));
        strokeRect(listX, listY, listW, H - 190, RED, 2f);

        assets.fontSmall.setColor(new Color(0.4f, 0.4f, 0.4f, 1f));
        drawText("MISSIONI DISPONIBILI", listX + 20, listY + H - 195);
        assets.fontSmall.setColor(Color.WHITE);

        float itemH = 90f;
        float iy = listY + H - 270;
        for (Mission m : missionSystem.getDailyMissions()) {
            boolean selected = m == missionSystem.getActiveMission();
            Color bg = m.done ? new Color(0.80f, 0.95f, 0.80f, 1f)
                    : (selected ? new Color(0.85f, 0.90f, 0.98f, 1f)
                                : Color.WHITE);
            fillRect(listX + 10, iy, listW - 20, itemH - 8, bg);
            strokeRect(listX + 10, iy, listW - 20, itemH - 8,
                    m.done ? GREEN : (selected ? BLUE : new Color(0.8f, 0.8f, 0.8f, 1f)), 1.5f);

            // Titolo
            assets.fontMedium.setColor(m.done ? GREEN : Color.BLACK);
            drawText((m.done ? "✓ " : "") + m.title, listX + 20, iy + itemH - 15);
            assets.fontMedium.setColor(Color.WHITE);

            // Descrizione
            assets.fontSmall.setColor(new Color(0.4f, 0.4f, 0.4f, 1f));
            drawTextSmall(m.description, listX + 20, iy + itemH - 38);
            assets.fontSmall.setColor(Color.WHITE);

            // Ricompensa
            assets.fontSmall.setColor(GOLD);
            drawTextSmall("+$" + m.rewardCoins + "  +" + m.rewardIngredientAmount + " " + m.ingredientType.displayName,
                    listX + 20, iy + itemH - 56);
            assets.fontSmall.setColor(Color.WHITE);

            // Progresso
            drawProgressBar(listX + 10, iy + 5, listW - 20, 8,
                    m.getProgressFraction(),
                    new Color(0.85f, 0.85f, 0.85f, 1f), GREEN);

            // Pulsante claim o selezione
            if (m.done && !m.claimed) {
                if (drawButton("RITIRA", listX + listW - 105, iy + 22, 88, 32, GOLD, Color.BLACK)) {
                    missionSystem.claimReward(m);
                }
            } else if (!m.done && !selected) {
                if (isTouched(listX + 10, iy, listW - 20, itemH - 8)) {
                    missionSystem.selectMission(m);
                    resetMiniTrackers();
                }
            }

            iy -= itemH;
            if (iy < listY) break;
        }

        // Completate
        assets.fontSmall.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
        drawText("Completate: " + missionSystem.countCompleted() + "/" + missionSystem.getDailyMissions().size(),
                listX + 20, listY + 18);
        assets.fontSmall.setColor(Color.WHITE);
    }

    // ---------------------------------------------------------------
    // Mini-gioco
    // ---------------------------------------------------------------

    private void drawMiniGame(float W, float H) {
        Mission m = missionSystem.getActiveMission();
        float areaX = 400f, areaY = 100f;
        float areaW = W - areaX - 30, areaH = H - 190;

        fillRect(areaX, areaY, areaW, areaH, new Color(0.99f, 0.97f, 0.94f, 1f));
        strokeRect(areaX, areaY, areaW, areaH, BLUE, 2f);

        float cx = areaX + areaW / 2f;

        if (m == null) {
            assets.fontMedium.setColor(new Color(0.6f, 0.6f, 0.6f, 1f));
            drawTextCentered("Seleziona una missione a sinistra", cx, areaY + areaH / 2f + 10);
            assets.fontMedium.setColor(Color.WHITE);
            return;
        }

        if (m.done) {
            assets.fontMedium.setColor(GREEN);
            drawTextCentered("Missione completata!", cx, areaY + areaH / 2f + 10);
            assets.fontMedium.setColor(Color.WHITE);
            return;
        }

        // Titolo missione
        assets.fontMedium.setColor(RED);
        drawTextCentered(m.title, cx, areaY + areaH - 20);
        assets.fontMedium.setColor(Color.WHITE);

        // Ingrediente colorato
        fillRect(cx - 60, areaY + areaH - 130, 120, 80,
                m.ingredientType.color);
        strokeRect(cx - 60, areaY + areaH - 130, 120, 80, Color.WHITE, 2f);
        assets.fontSmall.setColor(Color.WHITE);
        drawTextCentered(m.ingredientType.displayName, cx, areaY + areaH - 62);
        assets.fontSmall.setColor(Color.WHITE);

        // Istruzione
        assets.fontMedium.setColor(Color.BLACK);
        drawTextCentered(m.description, cx, areaY + areaH / 2f + 60);
        assets.fontMedium.setColor(Color.WHITE);

        // Icona step type
        String icon = getStepIcon(m.stepType);
        assets.fontLarge.setColor(RED);
        drawTextCenteredLarge(icon, cx, areaY + areaH / 2f + 10);
        assets.fontLarge.setColor(Color.WHITE);

        // Timer visivo per WAIT
        if (m.stepType == com.sushidays.entities.CookingStep.StepType.WAIT) {
            float fraction = (miniTimer % Constants.STEP_TIMEOUT) / Constants.STEP_TIMEOUT;
            drawProgressBar(areaX + 20, areaY + areaH / 2f - 30, areaW - 40, 20,
                    fraction, new Color(0.3f, 0.3f, 0.3f, 1f), RED);
            assets.fontSmall.setColor(Color.BLACK);
            drawTextCentered("Tocca al momento giusto!", cx, areaY + areaH / 2f - 38);
            assets.fontSmall.setColor(Color.WHITE);
        }

        // Progresso step richiesti
        drawProgressBar(areaX + 20, areaY + 55, areaW - 40, 18,
                m.getProgressFraction(), new Color(0.8f, 0.8f, 0.8f, 1f), GREEN);
        assets.fontSmall.setColor(new Color(0.4f, 0.4f, 0.4f, 1f));
        drawTextCentered(m.stepsCompleted + " / " + m.stepsRequired + " completati",
                cx, areaY + 48);
        assets.fontSmall.setColor(Color.WHITE);

        // Feedback punteggio
        if (stepFeedback > 0) {
            Color fc = lastScore >= 85 ? GREEN : lastScore >= 65 ? BLUE : lastScore >= 40 ? GOLD : RED;
            String fl = lastScore >= 85 ? "ECCELLENTE! " + lastScore
                    : lastScore >= 65 ? "BUONO " + lastScore
                    : lastScore >= 40 ? "PASSABILE " + lastScore
                    : "RIPROVA " + lastScore;
            assets.fontMedium.setColor(fc);
            drawTextCentered(fl, cx, areaY + 30);
            assets.fontMedium.setColor(Color.WHITE);
        }
    }

    private String getStepIcon(com.sushidays.entities.CookingStep.StepType type) {
        switch (type) {
            case SLICE: return "///";
            case ROLL:  return "()";
            case PRESS: return "V";
            case SHAKE: return "~~~";
            case POUR:  return "|";
            case WAIT:  return "( )";
            default:    return "?";
        }
    }

    // ---------------------------------------------------------------
    // Update & Input
    // ---------------------------------------------------------------

    private void update(float delta) {
        if (stepFeedback > 0) stepFeedback -= delta;
        miniTimer += delta;

        // Hold per PRESS
        Mission m = missionSystem.getActiveMission();
        if (m != null && holding) {
            holdTime += delta;
        }
    }

    private void onDown(float x, float y) {
        dragStartX = x; dragStartY = y;
        dragAccum  = 0f;
        rotAccum   = 0f;
        holdTime   = 0f;
        holding    = true;
    }

    private void onDrag(float x, float y) {
        dragAccum += Math.abs(x - dragStartX) + Math.abs(y - dragStartY);
        rotAccum  += 4f;
        dragStartX = x; dragStartY = y;
    }

    private void onUp(float x, float y) {
        holding = false;
        Mission m = missionSystem.getActiveMission();
        if (m == null || m.done) return;

        int score = evaluateMiniGame(m, x, y);
        lastScore    = score;
        stepFeedback = 1.5f;
        missionSystem.onStepCompleted(score);

        if (score >= 40) AudioManager.getInstance().playSuccess();
        else             AudioManager.getInstance().playError();
    }

    private int evaluateMiniGame(Mission m, float upX, float upY) {
        float totalTime = Constants.STEP_TIMEOUT;
        switch (m.stepType) {
            case SLICE: {
                float len = (float) Math.sqrt(Math.pow(upX - dragStartX, 2) + Math.pow(upY - dragStartY, 2));
                return Math.min(100, Math.round(Math.min(1f, len / 200f) * 100f));
            }
            case ROLL:
                return Math.min(100, Math.round(Math.min(1f, rotAccum / 360f) * 100f));
            case PRESS: {
                float opt = 2.5f;
                float diff = Math.abs(holdTime - opt);
                return Math.round(Math.max(0f, 1f - diff / opt) * 100f);
            }
            case SHAKE:
                return Math.min(100, Math.round(Math.min(1f, dragAccum / 600f) * 100f));
            case POUR: {
                float vert = Math.abs(upY - dragStartY);
                return Math.min(100, Math.round(Math.min(1f, vert / 150f) * 100f));
            }
            case WAIT: {
                float phase = (miniTimer % totalTime) / totalTime;
                float diff  = Math.abs(phase - 0.5f);
                return Math.round(Math.max(0f, 1f - diff / 0.25f) * 100f);
            }
            default: return 70;
        }
    }

    private void resetMiniTrackers() {
        dragAccum = 0f;
        rotAccum  = 0f;
        holdTime  = 0f;
        holding   = false;
    }

    @Override
    public void hide() {
        com.badlogic.gdx.Gdx.input.setInputProcessor(null);
    }
}
