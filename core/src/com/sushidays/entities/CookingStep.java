package com.sushidays.entities;

/**
 * Un singolo step di un mini-gioco di cucina.
 */
public class CookingStep {

    public enum StepType {
        SLICE,   // swipe veloce
        ROLL,    // movimento circolare
        PRESS,   // pressione prolungata (hold)
        SHAKE,   // agitazione rapida
        POUR,    // drag verticale controllato
        WAIT     // tap al momento giusto (timer)
    }

    public final StepType type;
    public final String   instruction;
    public final Ingredient.Type ingredient;

    // Risultato (compilato dopo l'esecuzione)
    public int    score    = 0;   // 0–100
    public boolean done    = false;

    public CookingStep(StepType type, String instruction, Ingredient.Type ingredient) {
        this.type        = type;
        this.instruction = instruction;
        this.ingredient  = ingredient;
    }

    public void complete(int score) {
        this.score = Math.max(0, Math.min(100, score));
        this.done  = true;
    }
}
