package com.sushidays.entities;

import com.badlogic.gdx.graphics.Color;
import com.sushidays.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class Customer {

    public enum CustomerType {
        NORMAL    ("Cliente",      new Color(0.40f, 0.70f, 0.90f, 1f), 1.0f, 1.0f),
        IMPATIENT ("Frettoloso",   new Color(0.90f, 0.40f, 0.20f, 1f), 0.6f, 1.2f),
        VIP       ("VIP",          new Color(0.80f, 0.60f, 0.90f, 1f), 1.4f, 1.5f),
        FOOD_CRITIC("Critico",     new Color(0.95f, 0.85f, 0.10f, 1f), 1.6f, 2.0f),
        TOURIST   ("Turista",      new Color(0.40f, 0.80f, 0.55f, 1f), 1.2f, 1.1f);

        public final String label;
        public final Color  color;
        public final float  patienceMultiplier;
        public final float  tipMultiplier;

        CustomerType(String label, Color color, float patienceMult, float tipMult) {
            this.label              = label;
            this.color              = color;
            this.patienceMultiplier = patienceMult;
            this.tipMultiplier      = tipMult;
        }
    }

    public enum CustomerState {
        ARRIVING,    // animazione entrata
        WAITING,     // attende che il piatto venga preparato
        EATING,      // sta mangiando (breve pausa)
        LEAVING,     // sta uscendo (soddisfatto o meno)
        GONE         // può essere rimosso dalla lista
    }

    private static int nextId = 1;

    public final int          id;
    public final CustomerType type;
    public final String       name;
    public CustomerState      state;

    public List<Recipe> order = new ArrayList<>();
    public int          orderIndex = 0;   // piatto corrente che sta aspettando

    public float        patience;          // secondi rimasti
    public final float  maxPatience;
    public float        satisfaction = 1f; // 0..1
    public int          totalEarned  = 0;

    // Posizione nel ristorante (usata dal renderer)
    public float x, y;
    public float targetX, targetY;

    // Timer per l'animazione di eating/leaving
    public float stateTimer = 0f;

    public Customer(CustomerType type, String name, float basePatience, List<Recipe> order) {
        this.id          = nextId++;
        this.type        = type;
        this.name        = name;
        this.maxPatience = basePatience * type.patienceMultiplier;
        this.patience    = this.maxPatience;
        this.order       = order;
        this.state       = CustomerState.ARRIVING;
    }

    // ---------------------------------------------------------------
    // Update
    // ---------------------------------------------------------------

    public void update(float delta) {
        stateTimer += delta;

        // Animazione arrivo
        if (state == CustomerState.ARRIVING) {
            float speed = 300f * delta;
            float dx = targetX - x;
            float dy = targetY - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist < speed) {
                x = targetX;
                y = targetY;
                state = CustomerState.WAITING;
                stateTimer = 0f;
            } else {
                x += dx / dist * speed;
                y += dy / dist * speed;
            }
            return;
        }

        if (state == CustomerState.WAITING) {
            patience -= delta;
            if (patience <= 0) {
                patience = 0;
                satisfaction = 0f;
                startLeaving(false);
            }
        }

        if (state == CustomerState.EATING) {
            if (stateTimer >= 2.5f) startLeaving(true);
        }

        if (state == CustomerState.LEAVING) {
            float speed = 250f * delta;
            x -= speed; // esce a sinistra
            if (x < -150) state = CustomerState.GONE;
        }
    }

    // ---------------------------------------------------------------
    // Azioni
    // ---------------------------------------------------------------

    /** Viene chiamato quando un piatto pronto viene consegnato a questo cliente. */
    public void receiveDish(Dish dish) {
        int earned = dish.calculateEarnings();
        totalEarned += earned;

        // Penalizza soddisfazione se il piatto è scadente
        switch (dish.quality) {
            case EXCELLENT: satisfaction = Math.min(1f, satisfaction + 0.10f); break;
            case GOOD:      /* nessuna variazione */ break;
            case PASSABLE:  satisfaction = Math.max(0f, satisfaction - 0.20f); break;
            case POOR:      satisfaction = Math.max(0f, satisfaction - 0.40f); break;
        }

        orderIndex++;
        if (orderIndex >= order.size()) {
            // Ha ricevuto tutti i piatti → mangia e va via
            state      = CustomerState.EATING;
            stateTimer = 0f;
        }
    }

    public void startLeaving(boolean satisfied) {
        state      = CustomerState.LEAVING;
        stateTimer = 0f;
        if (!satisfied) satisfaction = 0f;
    }

    // ---------------------------------------------------------------
    // Calcolo mancia
    // ---------------------------------------------------------------

    public int calculateTip() {
        float baseTip = totalEarned * 0.20f * type.tipMultiplier;
        float patFraction = patience / maxPatience;
        // Mancia bonus se ancora pieno di pazienza
        float speedBonus = patFraction > 0.5f ? 1.2f : 1.0f;
        return Math.round(baseTip * satisfaction * speedBonus);
    }

    // ---------------------------------------------------------------
    // Query
    // ---------------------------------------------------------------

    public float getPatienceFraction() {
        return maxPatience > 0 ? patience / maxPatience : 0f;
    }

    public Recipe currentDesiredRecipe() {
        if (orderIndex < order.size()) return order.get(orderIndex);
        return null;
    }

    public boolean isWaiting() { return state == CustomerState.WAITING; }
    public boolean isGone()    { return state == CustomerState.GONE;    }
}
