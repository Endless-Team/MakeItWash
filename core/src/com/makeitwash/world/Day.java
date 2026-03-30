package com.makeitwash.world;

public class Day {
    private int dayNumber;
    private float timeRemaining;
    private static final float DAY_DURATION = 180f;

    public Day() {
        this.dayNumber = 1;
        this.timeRemaining = DAY_DURATION;
    }

    public void start() {
        timeRemaining = DAY_DURATION;
    }

    public void update(float delta) {
        timeRemaining -= delta;
        if (timeRemaining < 0) {
            timeRemaining = 0;
        }
    }

    public boolean isFinished() {
        return timeRemaining <= 0;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void nextDay() {
        dayNumber++;
        start();
    }

    public float getTimeRemaining() {
        return timeRemaining;
    }

    public float getProgress() {
        return timeRemaining / DAY_DURATION;
    }
}
