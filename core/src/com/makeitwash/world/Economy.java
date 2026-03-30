package com.makeitwash.world;

public class Economy {
    private float yen;
    private float reputation;
    private static final float STARTING_YEN = 500f;
    private static final float STARTING_REPUTATION = 50f;

    public Economy() {
        this.yen = STARTING_YEN;
        this.reputation = STARTING_REPUTATION;
    }

    public void addYen(float amount) {
        yen += amount;
    }

    public boolean spendYen(float amount) {
        if (yen >= amount) {
            yen -= amount;
            return true;
        }
        return false;
    }

    public void addReputation(float amount) {
        reputation = Math.min(100f, reputation + amount);
    }

    public void loseReputation(float amount) {
        reputation = Math.max(0f, reputation - amount);
    }

    public float getYen() {
        return yen;
    }

    public float getReputation() {
        return reputation;
    }

    public boolean isBankrupt() {
        return yen <= 0 && reputation <= 0;
    }

    public void reset() {
        yen = STARTING_YEN;
        reputation = STARTING_REPUTATION;
    }
}
