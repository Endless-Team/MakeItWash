package com.sushidays.systems;

import com.sushidays.entities.Customer;
import com.sushidays.entities.Customer.CustomerType;
import com.sushidays.entities.Recipe;
import com.sushidays.utils.Constants;
import com.sushidays.utils.GameState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CustomerManager {

    private static final String[] NAMES = {
        "Yuki", "Hana", "Kenji", "Sakura", "Taro", "Aiko",
        "Marco", "Sofia", "Luca", "Emma", "Tom", "Mia",
        "Ren", "Nori", "Kai", "Sora", "Hiro", "Yuna"
    };

    private final GameState          state;
    private final DayManager         dayManager;
    private final List<Recipe>       availableRecipes;

    private final List<Customer>     customers     = new ArrayList<>();
    private final List<Customer>     toRemove      = new ArrayList<>();
    private final Random             rng           = new Random();

    private float spawnTimer   = 0f;
    private float spawnInterval;
    private int   spawnedToday = 0;

    // Posizioni slot clienti sullo schermo
    private static final float SLOT_Y      = 580f;
    private static final float SLOT_START_X = 80f;
    private static final float SLOT_SPACING = 220f;

    public CustomerManager(GameState state, DayManager dayManager, List<Recipe> availableRecipes) {
        this.state            = state;
        this.dayManager       = dayManager;
        this.availableRecipes = availableRecipes;
        this.spawnInterval    = state.getSpawnInterval();
    }

    // ---------------------------------------------------------------
    // Update
    // ---------------------------------------------------------------

    public void update(float delta) {
        // Aggiorna clienti esistenti
        for (Customer c : customers) c.update(delta);

        // Rimuovi clienti GONE
        toRemove.clear();
        for (Customer c : customers) {
            if (c.isGone()) toRemove.add(c);
        }
        customers.removeAll(toRemove);

        // Spawn nuovi clienti
        if (!dayManager.isDayOver()) {
            float effectiveInterval = spawnInterval / dayManager.getSpawnMultiplier();
            spawnTimer += delta;
            if (spawnTimer >= effectiveInterval) {
                spawnTimer = 0f;
                trySpawnCustomer();
            }
        }
    }

    // ---------------------------------------------------------------
    // Spawn
    // ---------------------------------------------------------------

    private void trySpawnCustomer() {
        int maxToday = state.getMaxCustomers() + spawnedToday / 3; // progressivo
        if (customers.size() >= Constants.MAX_CUSTOMERS_ON_SCREEN) return;
        if (spawnedToday >= maxToday) return;

        Customer c = createRandomCustomer();
        int slot   = getFreeSlot();
        c.targetX  = SLOT_START_X + slot * SLOT_SPACING;
        c.targetY  = SLOT_Y;
        c.x        = -100f; // entra da sinistra
        c.y        = SLOT_Y;

        customers.add(c);
        spawnedToday++;
        AudioManager.getInstance().playBell();
    }

    private int getFreeSlot() {
        // Trova lo slot più a sinistra libero
        boolean[] used = new boolean[Constants.MAX_CUSTOMERS_ON_SCREEN];
        for (Customer c : customers) {
            int slot = Math.round((c.targetX - SLOT_START_X) / SLOT_SPACING);
            if (slot >= 0 && slot < used.length) used[slot] = true;
        }
        for (int i = 0; i < used.length; i++) {
            if (!used[i]) return i;
        }
        return 0;
    }

    private Customer createRandomCustomer() {
        String name = NAMES[rng.nextInt(NAMES.length)];
        CustomerType type = rollCustomerType();
        List<Recipe> order = buildOrder(type);
        return new Customer(type, name, state.getCustomerPatience(), order);
    }

    private CustomerType rollCustomerType() {
        int day = state.currentDay;
        float roll = rng.nextFloat();

        // Food Critic ogni 5-7 giorni, solo 1 al giorno
        boolean criticDay = day >= 3 && day % 6 == 0;
        if (criticDay && spawnedToday == 0) return CustomerType.FOOD_CRITIC;

        if (roll < 0.50f) return CustomerType.NORMAL;
        if (roll < 0.70f) return CustomerType.IMPATIENT;
        if (roll < 0.85f && day >= 3) return CustomerType.VIP;
        if (roll < 0.95f && day >= 5) return CustomerType.TOURIST;
        return CustomerType.NORMAL;
    }

    private List<Recipe> buildOrder(CustomerType type) {
        List<Recipe> order = new ArrayList<>();
        if (availableRecipes.isEmpty()) return order;

        int count = 1;
        if (type == CustomerType.VIP) count = 1 + rng.nextInt(2);       // 1-2
        if (type == CustomerType.FOOD_CRITIC) count = 2 + rng.nextInt(2); // 2-3

        for (int i = 0; i < count; i++) {
            order.add(availableRecipes.get(rng.nextInt(availableRecipes.size())));
        }
        return order;
    }

    // ---------------------------------------------------------------
    // Interazione
    // ---------------------------------------------------------------

    /** Trova il cliente che sta aspettando un piatto con questa ricetta. */
    public Customer findWaitingCustomerFor(Recipe recipe) {
        for (Customer c : customers) {
            if (c.isWaiting()) {
                Recipe desired = c.currentDesiredRecipe();
                if (desired != null && desired.id.equals(recipe.id)) return c;
            }
        }
        return null;
    }

    /** Restituisce il cliente toccato in base alle coordinate dello schermo. */
    public Customer getCustomerAt(float x, float y) {
        for (Customer c : customers) {
            if (c.isWaiting() || c.state == Customer.CustomerState.EATING) {
                float hw = 50f, hh = 90f;
                if (x >= c.x - hw && x <= c.x + hw &&
                    y >= c.y - hh && y <= c.y + hh) {
                    return c;
                }
            }
        }
        return null;
    }

    public void notifyCustomerLost(Customer c) {
        dayManager.recordLost();
    }

    public List<Customer> getCustomers() { return customers; }

    public boolean allGone() {
        return customers.isEmpty();
    }
}
