package com.sushidays.entities;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public class Recipe {

    public enum Quality {
        EXCELLENT (1.50f, "Eccellente!", new Color(0.20f, 0.80f, 0.30f, 1f)),
        GOOD      (1.15f, "Buono",       new Color(0.25f, 0.60f, 0.85f, 1f)),
        PASSABLE  (0.80f, "Passabile",   new Color(0.90f, 0.75f, 0.10f, 1f)),
        POOR      (0.40f, "Scadente",    new Color(0.85f, 0.25f, 0.15f, 1f));

        public final float  earningsMultiplier;
        public final String label;
        public final Color  color;

        Quality(float mult, String label, Color color) {
            this.earningsMultiplier = mult;
            this.label = label;
            this.color = color;
        }
    }

    public final String id;
    public final String displayName;
    public final int    basePrice;
    public final int    unlockDay;
    public final Color  dishColor;
    public final List<CookingStep> steps;

    public Recipe(String id, String displayName, int basePrice, int unlockDay, Color dishColor) {
        this.id          = id;
        this.displayName = displayName;
        this.basePrice   = basePrice;
        this.unlockDay   = unlockDay;
        this.dishColor   = dishColor;
        this.steps       = new ArrayList<>();
    }

    public Recipe addStep(CookingStep step) {
        steps.add(step);
        return this;
    }

    /** Calcola la qualità dalla media degli score degli step. */
    public static Quality evaluateQuality(List<CookingStep> steps) {
        if (steps.isEmpty()) return Quality.POOR;
        int total = 0;
        for (CookingStep s : steps) total += s.score;
        int avg = total / steps.size();
        if (avg >= 85) return Quality.EXCELLENT;
        if (avg >= 65) return Quality.GOOD;
        if (avg >= 40) return Quality.PASSABLE;
        return Quality.POOR;
    }

    /** Ritorna una copia fresca degli step per una nuova preparazione. */
    public List<CookingStep> freshSteps() {
        List<CookingStep> fresh = new ArrayList<>();
        for (CookingStep s : steps) {
            fresh.add(new CookingStep(s.type, s.instruction, s.ingredient));
        }
        return fresh;
    }
}
