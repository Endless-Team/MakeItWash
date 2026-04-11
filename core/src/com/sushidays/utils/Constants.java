package com.sushidays.utils;

public final class Constants {

    private Constants() {}

    // --- Viewport ---
    public static final float WORLD_WIDTH  = 1280f;
    public static final float WORLD_HEIGHT = 720f;

    // --- Gameplay ---
    public static final int   MAX_CUSTOMERS_ON_SCREEN = 5;
    public static final float BASE_DAY_DURATION        = 180f;  // secondi
    public static final float BASE_CUSTOMER_PATIENCE   = 30f;   // secondi
    public static final float RUSH_HOUR_START_PERCENT  = 0.60f; // 60% del timer
    public static final float DIFFICULTY_INCREMENT     = 0.15f; // per giorno
    public static final float SPAWN_INTERVAL_BASE      = 12f;   // secondi tra clienti

    // --- Economy ---
    public static final int STARTING_COINS          = 50;
    public static final int PENALTY_LOST_CUSTOMER   = 5;
    public static final int BONUS_PERFECT_DAY       = 30;
    public static final float SATISFACTION_BONUS_MULT = 1.20f;

    // --- Cooking ---
    public static final float STEP_TIMEOUT          = 8f;   // secondi per completare uno step
    public static final int   EXCELLENT_THRESHOLD   = 85;
    public static final int   GOOD_THRESHOLD        = 65;
    public static final int   PASSABLE_THRESHOLD    = 40;

    // --- UI ---
    public static final float ANIM_DURATION         = 0.25f;
    public static final float HUD_PADDING           = 16f;

    // --- Save keys ---
    public static final String PREF_NAME            = "sushidays_save";
    public static final String SAVE_DAY             = "current_day";
    public static final String SAVE_COINS           = "coins";
    public static final String SAVE_UPGRADES        = "upgrades";
    public static final String SAVE_COSMETICS       = "cosmetics";
    public static final String SAVE_TOTAL_SERVED    = "total_served";
    public static final String SAVE_MUSIC_VOL       = "music_vol";
    public static final String SAVE_SFX_VOL         = "sfx_vol";

    // --- Colors (packed RGBA8888) ---
    public static final float[] COLOR_BG            = {0.96f, 0.93f, 0.88f, 1f};
    public static final float[] COLOR_ACCENT        = {0.85f, 0.20f, 0.15f, 1f};
    public static final float[] COLOR_CUSTOMER_NORMAL   = {0.40f, 0.70f, 0.90f, 1f};
    public static final float[] COLOR_CUSTOMER_IMPATIENT = {0.90f, 0.40f, 0.20f, 1f};
    public static final float[] COLOR_CUSTOMER_VIP   = {0.80f, 0.60f, 0.90f, 1f};
    public static final float[] COLOR_PATIENCE_HIGH  = {0.25f, 0.75f, 0.30f, 1f};
    public static final float[] COLOR_PATIENCE_MED   = {0.95f, 0.75f, 0.10f, 1f};
    public static final float[] COLOR_PATIENCE_LOW   = {0.90f, 0.20f, 0.15f, 1f};
    public static final float[] COLOR_COIN           = {0.95f, 0.78f, 0.10f, 1f};
}
