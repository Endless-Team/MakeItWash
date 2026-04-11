package com.sushidays.entities;

import com.badlogic.gdx.graphics.Color;

public class Ingredient {

    public enum Type {
        RICE        ("Riso",       new Color(0.98f, 0.96f, 0.88f, 1f)),
        NORI        ("Nori",       new Color(0.15f, 0.20f, 0.10f, 1f)),
        SALMON      ("Salmone",    new Color(0.95f, 0.55f, 0.35f, 1f)),
        TUNA        ("Tonno",      new Color(0.80f, 0.25f, 0.25f, 1f)),
        SHRIMP      ("Gamberetti", new Color(0.95f, 0.70f, 0.60f, 1f)),
        AVOCADO     ("Avocado",    new Color(0.50f, 0.75f, 0.30f, 1f)),
        CUCUMBER    ("Cetriolo",   new Color(0.35f, 0.70f, 0.40f, 1f)),
        SOY_SAUCE   ("Salsa soia", new Color(0.30f, 0.15f, 0.05f, 1f)),
        WASABI      ("Wasabi",     new Color(0.40f, 0.80f, 0.35f, 1f)),
        SALMON_ROE  ("Uova salm.", new Color(0.95f, 0.40f, 0.10f, 1f)),
        EEL         ("Anguilla",   new Color(0.45f, 0.30f, 0.15f, 1f));

        public final String  displayName;
        public final Color   color;

        Type(String displayName, Color color) {
            this.displayName = displayName;
            this.color       = color;
        }
    }

    public final Type type;
    public int quantity;
    public boolean unlocked;

    public Ingredient(Type type, int quantity, boolean unlocked) {
        this.type     = type;
        this.quantity = quantity;
        this.unlocked = unlocked;
    }

    public boolean consume(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
            return true;
        }
        return false;
    }

    public void restock(int amount) {
        quantity += amount;
    }
}
