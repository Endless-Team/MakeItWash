package com.sushidays.entities;

import java.util.List;

/**
 * Un piatto in preparazione o pronto per la consegna.
 */
public class Dish {

    public enum State {
        QUEUED,      // in attesa di essere preparato
        COOKING,     // mini-gioco in corso
        READY,       // pronto da consegnare
        DELIVERED,   // consegnato
        FAILED       // tempo scaduto o ingredienti mancanti
    }

    public final Recipe recipe;
    public State state = State.QUEUED;
    public Recipe.Quality quality;

    // Step corrente durante la preparazione
    public List<CookingStep> activeSteps;
    public int currentStepIndex = 0;

    // Tempo per il timeout dello step corrente
    public float stepTimer = 0f;

    public Dish(Recipe recipe) {
        this.recipe      = recipe;
        this.activeSteps = recipe.freshSteps();
    }

    public CookingStep currentStep() {
        if (currentStepIndex < activeSteps.size()) {
            return activeSteps.get(currentStepIndex);
        }
        return null;
    }

    public boolean isLastStep() {
        return currentStepIndex >= activeSteps.size() - 1;
    }

    public void advanceStep() {
        currentStepIndex++;
        stepTimer = 0f;
    }

    /** Finalizza il piatto calcolando la qualità. */
    public void finalize(boolean success) {
        if (success) {
            quality = Recipe.evaluateQuality(activeSteps);
            state   = State.READY;
        } else {
            quality = Recipe.Quality.POOR;
            state   = State.FAILED;
        }
    }

    public int calculateEarnings() {
        if (quality == null) return 0;
        return Math.round(recipe.basePrice * quality.earningsMultiplier);
    }
}
