package com.sushidays.systems;

import com.badlogic.gdx.graphics.Color;
import com.sushidays.entities.CookingStep;
import com.sushidays.entities.CookingStep.StepType;
import com.sushidays.entities.Ingredient.Type;
import com.sushidays.entities.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Registro centralizzato di tutte le ricette disponibili nel gioco.
 * Le ricette vengono sbloccate in base al giorno corrente.
 */
public class RecipeRegistry {

    private static final List<Recipe> ALL_RECIPES = new ArrayList<>();

    static {
        // ============================================================
        // RICETTE BASE (giorno 1)
        // ============================================================

        Recipe nigiriSalmone = new Recipe(
                "nigiri_salmon", "Nigiri Salmone", 15, 1,
                new Color(0.95f, 0.55f, 0.35f, 1f))
            .addStep(new CookingStep(StepType.PRESS,  "Compatta il riso!", Type.RICE))
            .addStep(new CookingStep(StepType.SLICE,  "Taglia il salmone!", Type.SALMON))
            .addStep(new CookingStep(StepType.PRESS,  "Adagia il pesce sul riso!", Type.SALMON));
        ALL_RECIPES.add(nigiriSalmone);

        Recipe makiCetriolo = new Recipe(
                "maki_cucumber", "Maki Cetriolo", 12, 1,
                new Color(0.35f, 0.70f, 0.40f, 1f))
            .addStep(new CookingStep(StepType.PRESS,  "Stendi il riso sul nori!", Type.RICE))
            .addStep(new CookingStep(StepType.SLICE,  "Taglia il cetriolo!", Type.CUCUMBER))
            .addStep(new CookingStep(StepType.ROLL,   "Arrotola il maki!", Type.NORI))
            .addStep(new CookingStep(StepType.SLICE,  "Taglia le fette!", Type.NORI));
        ALL_RECIPES.add(makiCetriolo);

        Recipe temakiBase = new Recipe(
                "temaki_base", "Temaki Misto", 18, 1,
                new Color(0.50f, 0.75f, 0.30f, 1f))
            .addStep(new CookingStep(StepType.PRESS,  "Stendi il riso!", Type.RICE))
            .addStep(new CookingStep(StepType.SLICE,  "Taglia avocado e salmone!", Type.AVOCADO))
            .addStep(new CookingStep(StepType.ROLL,   "Forma il cono!", Type.NORI));
        ALL_RECIPES.add(temakiBase);

        // ============================================================
        // RICETTE GIORNO 4
        // ============================================================

        Recipe nigiriTonno = new Recipe(
                "nigiri_tuna", "Nigiri Tonno", 18, 4,
                new Color(0.80f, 0.25f, 0.25f, 1f))
            .addStep(new CookingStep(StepType.PRESS,  "Compatta il riso!", Type.RICE))
            .addStep(new CookingStep(StepType.SLICE,  "Taglia il tonno!", Type.TUNA))
            .addStep(new CookingStep(StepType.PRESS,  "Adagia il tonno!", Type.TUNA))
            .addStep(new CookingStep(StepType.POUR,   "Aggiungi la salsa di soia!", Type.SOY_SAUCE));
        ALL_RECIPES.add(nigiriTonno);

        Recipe makiGamberetti = new Recipe(
                "maki_shrimp", "Maki Gamberetti", 20, 4,
                new Color(0.95f, 0.70f, 0.60f, 1f))
            .addStep(new CookingStep(StepType.WAIT,   "Cuoci i gamberetti!", Type.SHRIMP))
            .addStep(new CookingStep(StepType.PRESS,  "Stendi il riso!", Type.RICE))
            .addStep(new CookingStep(StepType.SLICE,  "Taglia i gamberetti!", Type.SHRIMP))
            .addStep(new CookingStep(StepType.ROLL,   "Arrotola il maki!", Type.NORI))
            .addStep(new CookingStep(StepType.SLICE,  "Affetta!", Type.NORI));
        ALL_RECIPES.add(makiGamberetti);

        // ============================================================
        // RICETTE GIORNO 8
        // ============================================================

        Recipe chirashi = new Recipe(
                "chirashi", "Chirashi Bowl", 35, 8,
                new Color(0.95f, 0.78f, 0.30f, 1f))
            .addStep(new CookingStep(StepType.WAIT,   "Cuoci il riso!", Type.RICE))
            .addStep(new CookingStep(StepType.SLICE,  "Taglia il salmone!", Type.SALMON))
            .addStep(new CookingStep(StepType.SLICE,  "Taglia il tonno!", Type.TUNA))
            .addStep(new CookingStep(StepType.SLICE,  "Taglia l'avocado!", Type.AVOCADO))
            .addStep(new CookingStep(StepType.POUR,   "Condisci con soia!", Type.SOY_SAUCE))
            .addStep(new CookingStep(StepType.PRESS,  "Disponi il pesce!", Type.SALMON));
        ALL_RECIPES.add(chirashi);

        Recipe uramaki = new Recipe(
                "uramaki_av_sal", "Uramaki Avocado Salmone", 28, 8,
                new Color(0.50f, 0.70f, 0.55f, 1f))
            .addStep(new CookingStep(StepType.PRESS,  "Stendi il riso sul nori!", Type.RICE))
            .addStep(new CookingStep(StepType.SHAKE,  "Cospargi di sesamo!", Type.RICE))
            .addStep(new CookingStep(StepType.SLICE,  "Taglia avocado e salmone!", Type.AVOCADO))
            .addStep(new CookingStep(StepType.ROLL,   "Arrotola inside-out!", Type.NORI))
            .addStep(new CookingStep(StepType.SLICE,  "Affetta in 8 pezzi!", Type.NORI));
        ALL_RECIPES.add(uramaki);

        Recipe nigiriGamberetti = new Recipe(
                "nigiri_shrimp", "Nigiri Gamberetti", 22, 8,
                new Color(0.95f, 0.65f, 0.55f, 1f))
            .addStep(new CookingStep(StepType.WAIT,   "Cuoci i gamberetti!", Type.SHRIMP))
            .addStep(new CookingStep(StepType.PRESS,  "Compatta il riso!", Type.RICE))
            .addStep(new CookingStep(StepType.PRESS,  "Adagia il gamberetto!", Type.SHRIMP));
        ALL_RECIPES.add(nigiriGamberetti);

        // ============================================================
        // RICETTE SPECIALI (giorno 16+)
        // ============================================================

        Recipe omakase = new Recipe(
                "omakase", "Omakase del Chef", 80, 16,
                new Color(0.85f, 0.75f, 0.20f, 1f))
            .addStep(new CookingStep(StepType.WAIT,   "Cuoci il riso a vapore!", Type.RICE))
            .addStep(new CookingStep(StepType.SLICE,  "Taglia il tonno pregiato!", Type.TUNA))
            .addStep(new CookingStep(StepType.SLICE,  "Taglia il salmone!", Type.SALMON))
            .addStep(new CookingStep(StepType.PRESS,  "Forma i nigiri!", Type.RICE))
            .addStep(new CookingStep(StepType.POUR,   "Aggiungi wasabi!", Type.WASABI))
            .addStep(new CookingStep(StepType.ROLL,   "Arrotola il maki speciale!", Type.NORI))
            .addStep(new CookingStep(StepType.POUR,   "Guarnisci con uova di salmone!", Type.SALMON_ROE));
        ALL_RECIPES.add(omakase);
    }

    /** Tutte le ricette disponibili per il giorno dato. */
    public static List<Recipe> getAvailableRecipes(int day) {
        List<Recipe> available = new ArrayList<>();
        for (Recipe r : ALL_RECIPES) {
            if (r.unlockDay <= day) available.add(r);
        }
        return available;
    }

    public static Recipe getById(String id) {
        for (Recipe r : ALL_RECIPES) {
            if (r.id.equals(id)) return r;
        }
        return null;
    }

    public static List<Recipe> getAllRecipes() {
        return new ArrayList<>(ALL_RECIPES);
    }
}
