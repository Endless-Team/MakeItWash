package com.sushidays.systems;

import com.sushidays.entities.CookingStep.StepType;
import com.sushidays.entities.Ingredient.Type;
import com.sushidays.utils.GameState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Gestisce la generazione e il completamento delle missioni secondarie
 * che il giocatore può svolgere tra un giorno e l'altro.
 */
public class MissionSystem {

    public static class Mission {
        public final String   id;
        public final String   title;
        public final String   description;
        public final StepType stepType;
        public final Type     ingredientType;
        public final int      rewardIngredientAmount;
        public final int      rewardCoins;
        public final int      stepsRequired;  // quante volte eseguire lo step

        // Stato
        public int   stepsCompleted = 0;
        public boolean done          = false;
        public boolean claimed       = false;

        public Mission(String id, String title, String description,
                       StepType stepType, Type ingredientType,
                       int stepsRequired, int rewardIngredientAmount, int rewardCoins) {
            this.id                    = id;
            this.title                 = title;
            this.description           = description;
            this.stepType              = stepType;
            this.ingredientType        = ingredientType;
            this.stepsRequired         = stepsRequired;
            this.rewardIngredientAmount = rewardIngredientAmount;
            this.rewardCoins           = rewardCoins;
        }

        public float getProgressFraction() {
            return stepsRequired > 0 ? (float) stepsCompleted / stepsRequired : 0f;
        }

        public void recordStep(int score) {
            if (done) return;
            if (score >= 40) stepsCompleted++;
            if (stepsCompleted >= stepsRequired) done = true;
        }
    }

    private static final List<Mission> MISSION_POOL = new ArrayList<>();

    static {
        MISSION_POOL.add(new Mission("cook_rice",   "Cuoci il Riso Perfetto",
                "Ferma il timer al momento giusto!",
                StepType.WAIT, Type.RICE, 1, 30, 20));

        MISSION_POOL.add(new Mission("slice_salmon", "Taglia il Salmone",
                "5 tagli precisi sul salmone fresco!",
                StepType.SLICE, Type.SALMON, 5, 20, 15));

        MISSION_POOL.add(new Mission("roll_maki",   "Arrotola i Maki",
                "4 rotoli perfetti per domani!",
                StepType.ROLL, Type.NORI, 4, 15, 30));

        MISSION_POOL.add(new Mission("slice_tuna",  "Prepara il Tonno",
                "Affetta il tonno in modo preciso!",
                StepType.SLICE, Type.TUNA, 4, 18, 18));

        MISSION_POOL.add(new Mission("pour_soy",    "Prepara la Marinatura",
                "Versa la salsa con delicatezza!",
                StepType.POUR, Type.SOY_SAUCE, 3, 20, 15));

        MISSION_POOL.add(new Mission("shake_sesame","Sesamo Tostato",
                "Scuoti il sesamo fino a tostarlo!",
                StepType.SHAKE, Type.RICE, 2, 10, 25));

        MISSION_POOL.add(new Mission("press_rice",  "Riso Compattato",
                "Premi il riso per i nigiri!",
                StepType.PRESS, Type.RICE, 3, 25, 20));

        MISSION_POOL.add(new Mission("slice_avocado","Taglia l'Avocado",
                "Affetta l'avocado a fette sottili!",
                StepType.SLICE, Type.AVOCADO, 4, 20, 12));
    }

    private final GameState        state;
    private final InventorySystem  inventory;
    private final Random           rng = new Random();

    private final List<Mission> dailyMissions = new ArrayList<>();
    private Mission activeMission = null;

    public MissionSystem(GameState state, InventorySystem inventory) {
        this.state     = state;
        this.inventory = inventory;
    }

    // ---------------------------------------------------------------
    // Generazione missioni giornaliere
    // ---------------------------------------------------------------

    public void generateDailyMissions() {
        dailyMissions.clear();
        activeMission = null;

        // 2 missioni nei primi giorni, 3 dal giorno 5
        int count = state.currentDay >= 5 ? 3 : 2;
        count = Math.min(count, MISSION_POOL.size());

        List<Mission> shuffled = new ArrayList<>(MISSION_POOL);
        Collections.shuffle(shuffled, rng);

        for (int i = 0; i < count; i++) {
            Mission template = shuffled.get(i);
            // Crea una copia fresca della missione
            Mission fresh = new Mission(
                template.id, template.title, template.description,
                template.stepType, template.ingredientType,
                template.stepsRequired, template.rewardIngredientAmount, template.rewardCoins
            );
            dailyMissions.add(fresh);
        }
    }

    // ---------------------------------------------------------------
    // Interazione
    // ---------------------------------------------------------------

    public void selectMission(Mission m) {
        if (!m.done && !m.claimed) activeMission = m;
    }

    /** Chiamato dalla MissionScreen quando l'utente completa un'interazione. */
    public void onStepCompleted(int score) {
        if (activeMission == null || activeMission.done) return;
        activeMission.recordStep(score);
    }

    /** Riscuote la ricompensa di una missione completata. */
    public boolean claimReward(Mission m) {
        if (!m.done || m.claimed) return false;
        m.claimed = true;
        state.addCoins(m.rewardCoins);
        inventory.restock(m.ingredientType, m.rewardIngredientAmount);
        AudioManager.getInstance().playCoin();
        return true;
    }

    public List<Mission> getDailyMissions() { return dailyMissions; }
    public Mission       getActiveMission()  { return activeMission; }

    public boolean allMissionsDone() {
        for (Mission m : dailyMissions) {
            if (!m.done) return false;
        }
        return true;
    }

    public int countCompleted() {
        int c = 0;
        for (Mission m : dailyMissions) if (m.done) c++;
        return c;
    }
}
