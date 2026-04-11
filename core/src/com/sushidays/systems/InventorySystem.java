package com.sushidays.systems;

import com.sushidays.entities.Ingredient;
import com.sushidays.entities.Ingredient.Type;

import java.util.EnumMap;
import java.util.Map;

public class InventorySystem {

    private final Map<Type, Ingredient> inventory = new EnumMap<>(Type.class);

    public InventorySystem() {
        // Ingredienti base sempre disponibili dal giorno 1
        add(Type.RICE,      50, true);
        add(Type.NORI,      30, true);
        add(Type.SALMON,    20, true);
        add(Type.TUNA,      15, true);
        add(Type.SHRIMP,    15, true);
        add(Type.AVOCADO,   20, true);
        add(Type.CUCUMBER,  25, true);
        add(Type.SOY_SAUCE, 40, true);
        add(Type.WASABI,    30, true);
        // Speciali — sbloccati con upgrade
        add(Type.SALMON_ROE, 0, false);
        add(Type.EEL,        0, false);
    }

    private void add(Type type, int qty, boolean unlocked) {
        inventory.put(type, new Ingredient(type, qty, unlocked));
    }

    public Ingredient get(Type type) {
        return inventory.get(type);
    }

    public boolean consume(Type type, int amount) {
        Ingredient ing = inventory.get(type);
        return ing != null && ing.unlocked && ing.consume(amount);
    }

    public void restock(Type type, int amount) {
        Ingredient ing = inventory.get(type);
        if (ing != null) ing.restock(amount);
    }

    public void unlock(Type type) {
        Ingredient ing = inventory.get(type);
        if (ing != null) ing.unlocked = true;
    }

    /** Ricarica parziale automatica a inizio giornata. */
    public void dailyRestock(int day) {
        int base = 10 + day * 2;
        restock(Type.RICE,      base + 20);
        restock(Type.NORI,      base + 10);
        restock(Type.SALMON,    base);
        restock(Type.TUNA,      base - 2);
        restock(Type.SHRIMP,    base - 2);
        restock(Type.AVOCADO,   base + 5);
        restock(Type.CUCUMBER,  base + 5);
        restock(Type.SOY_SAUCE, base + 15);
        restock(Type.WASABI,    base + 10);
        if (inventory.get(Type.SALMON_ROE).unlocked) restock(Type.SALMON_ROE, 5);
        if (inventory.get(Type.EEL).unlocked)        restock(Type.EEL,        5);
    }

    public boolean hasIngredient(Type type) {
        Ingredient ing = inventory.get(type);
        return ing != null && ing.unlocked && ing.quantity > 0;
    }

    public Map<Type, Ingredient> getAll() {
        return inventory;
    }
}
