package com.sushidays.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.sushidays.entities.Upgrade;

import java.util.HashSet;
import java.util.Set;

/**
 * Contenitore dello stato persistente del gioco.
 * Viene creato una volta in SushiDaysGame e passato a tutte le Screen.
 */
public class GameState {

    public int   currentDay;
    public int   coins;
    public int   totalServed;
    public int   totalDaysPlayed;
    public float musicVolume;
    public float sfxVolume;

    // Upgrade acquistati (id string)
    public Set<String> purchasedUpgrades  = new HashSet<>();
    // Cosmetici acquistati
    public Set<String> purchasedCosmetics = new HashSet<>();
    // Cosmetico attivo
    public String activeCosmetic = "default";

    // Upgrade funzionali calcolati da purchasedUpgrades
    public float sliceSpeedBonus   = 0f;  // 0..1
    public boolean hasExtraCooking = false;
    public boolean hasDeluxeTray   = false;
    public float patienceBonus     = 0f;  // secondi extra

    public GameState() {
        load();
    }

    // ---------------------------------------------------------------
    // Salvataggio / Caricamento
    // ---------------------------------------------------------------

    public void save() {
        Preferences prefs = Gdx.app.getPreferences(Constants.PREF_NAME);
        prefs.putInteger(Constants.SAVE_DAY,   currentDay);
        prefs.putInteger(Constants.SAVE_COINS, coins);
        prefs.putInteger(Constants.SAVE_TOTAL_SERVED, totalServed);
        prefs.putFloat(Constants.SAVE_MUSIC_VOL, musicVolume);
        prefs.putFloat(Constants.SAVE_SFX_VOL,   sfxVolume);
        prefs.putString(Constants.SAVE_UPGRADES,  String.join(",", purchasedUpgrades));
        prefs.putString(Constants.SAVE_COSMETICS, String.join(",", purchasedCosmetics));
        prefs.putString("active_cosmetic", activeCosmetic);
        prefs.flush();
    }

    public final void load() {
        Preferences prefs = Gdx.app.getPreferences(Constants.PREF_NAME);
        currentDay   = prefs.getInteger(Constants.SAVE_DAY,   1);
        coins        = prefs.getInteger(Constants.SAVE_COINS, Constants.STARTING_COINS);
        totalServed  = prefs.getInteger(Constants.SAVE_TOTAL_SERVED, 0);
        musicVolume  = prefs.getFloat(Constants.SAVE_MUSIC_VOL, 0.7f);
        sfxVolume    = prefs.getFloat(Constants.SAVE_SFX_VOL,   0.8f);
        activeCosmetic = prefs.getString("active_cosmetic", "default");

        String upStr = prefs.getString(Constants.SAVE_UPGRADES, "");
        purchasedUpgrades.clear();
        if (!upStr.isEmpty()) {
            for (String s : upStr.split(",")) purchasedUpgrades.add(s.trim());
        }

        String cosStr = prefs.getString(Constants.SAVE_COSMETICS, "");
        purchasedCosmetics.clear();
        if (!cosStr.isEmpty()) {
            for (String s : cosStr.split(",")) purchasedCosmetics.add(s.trim());
        }

        recalculateUpgrades();
    }

    public void reset() {
        Preferences prefs = Gdx.app.getPreferences(Constants.PREF_NAME);
        prefs.clear();
        prefs.flush();
        currentDay   = 1;
        coins        = Constants.STARTING_COINS;
        totalServed  = 0;
        purchasedUpgrades.clear();
        purchasedCosmetics.clear();
        activeCosmetic = "default";
        recalculateUpgrades();
    }

    // ---------------------------------------------------------------
    // Economia
    // ---------------------------------------------------------------

    public boolean spendCoins(int amount) {
        if (coins >= amount) {
            coins -= amount;
            return true;
        }
        return false;
    }

    public void addCoins(int amount) {
        coins += amount;
    }

    public boolean hasUpgrade(String id) {
        return purchasedUpgrades.contains(id);
    }

    public void purchaseUpgrade(Upgrade u) {
        purchasedUpgrades.add(u.id);
        recalculateUpgrades();
    }

    public void purchaseCosmetic(String id) {
        purchasedCosmetics.add(id);
    }

    /** Ricalcola i bonus derivati dagli upgrade acquistati. */
    public void recalculateUpgrades() {
        sliceSpeedBonus   = 0f;
        hasExtraCooking   = false;
        hasDeluxeTray     = false;
        patienceBonus     = 0f;

        for (String id : purchasedUpgrades) {
            switch (id) {
                case "knife_better":   sliceSpeedBonus   += 0.15f; break;
                case "knife_pro":      sliceSpeedBonus   += 0.30f; break;
                case "extra_cooking":  hasExtraCooking    = true;  break;
                case "deluxe_tray":    hasDeluxeTray      = true;  break;
                case "bell":           patienceBonus     += 5f;    break;
            }
        }
    }

    // ---------------------------------------------------------------
    // Difficoltà
    // ---------------------------------------------------------------

    public float getDifficultyScale() {
        return 1f + (currentDay - 1) * Constants.DIFFICULTY_INCREMENT;
    }

    public int getMaxCustomers() {
        int base = 4;
        if (currentDay >= 4)  base = 6;
        if (currentDay >= 8)  base = 9;
        if (currentDay >= 16) base = 12;
        return Math.min(base, Constants.MAX_CUSTOMERS_ON_SCREEN);
    }

    public float getCustomerPatience() {
        float patience = Constants.BASE_CUSTOMER_PATIENCE - (currentDay - 1) * 0.8f;
        patience = Math.max(patience, 12f) + patienceBonus;
        return patience;
    }

    public float getSpawnInterval() {
        float interval = Constants.SPAWN_INTERVAL_BASE / getDifficultyScale();
        return Math.max(interval, 4f);
    }

    public float getDayDuration() {
        return Constants.BASE_DAY_DURATION;
    }
}
