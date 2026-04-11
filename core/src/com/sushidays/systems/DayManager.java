package com.sushidays.systems;

import com.sushidays.utils.Constants;
import com.sushidays.utils.GameState;

/**
 * Gestisce il ciclo di una singola giornata di gioco.
 * Tiene traccia della fase corrente, del timer e delle statistiche.
 */
public class DayManager {

    public enum DayPhase {
        MORNING_MISSION,  // preparazione ingredienti (opzionale)
        SERVICE,          // servizio normale
        RUSH_HOUR,        // rush hour (più clienti, più veloce)
        DAY_END           // giornata terminata
    }

    private final GameState state;

    private DayPhase phase        = DayPhase.SERVICE;
    private float    dayTimer     = 0f;   // tempo trascorso
    private float    dayDuration  = 0f;   // durata totale giornata
    private boolean  rushTriggered = false;

    // Statistiche della giornata corrente
    public int  customersServed   = 0;
    public int  customersLost     = 0;
    public int  dishesCooked      = 0;
    public int  coinsEarned       = 0;
    public int  mistakesCount     = 0;
    public float avgSatisfaction  = 0f;
    private int  satisfactionSamples = 0;

    public DayManager(GameState state) {
        this.state       = state;
        this.dayDuration = state.getDayDuration();
    }

    // ---------------------------------------------------------------
    // Update
    // ---------------------------------------------------------------

    public void update(float delta) {
        if (phase == DayPhase.DAY_END || phase == DayPhase.MORNING_MISSION) return;

        dayTimer += delta;

        // Trigger rush hour
        if (!rushTriggered && getProgressFraction() >= Constants.RUSH_HOUR_START_PERCENT) {
            rushTriggered = true;
            phase = DayPhase.RUSH_HOUR;
            AudioManager.getInstance().playRushMusic();
        }

        // Fine giornata
        if (dayTimer >= dayDuration) {
            dayTimer = dayDuration;
            phase    = DayPhase.DAY_END;
        }
    }

    // ---------------------------------------------------------------
    // Controllo fasi
    // ---------------------------------------------------------------

    public void startService() {
        phase          = DayPhase.SERVICE;
        dayTimer       = 0f;
        rushTriggered  = false;
        AudioManager.getInstance().playGameMusic();
    }

    public void forceEndDay() {
        phase    = DayPhase.DAY_END;
        dayTimer = dayDuration;
    }

    // ---------------------------------------------------------------
    // Statistiche
    // ---------------------------------------------------------------

    public void recordServed(int coins, float satisfaction) {
        customersServed++;
        coinsEarned += coins;
        avgSatisfaction = (avgSatisfaction * satisfactionSamples + satisfaction)
                          / (satisfactionSamples + 1);
        satisfactionSamples++;
    }

    public void recordLost() {
        customersLost++;
        coinsEarned -= Constants.PENALTY_LOST_CUSTOMER;
    }

    public void recordDish() {
        dishesCooked++;
    }

    public void recordMistake() {
        mistakesCount++;
    }

    /** Calcola il bonus di fine giornata in monete. */
    public int calculateDayBonus() {
        int bonus = 0;
        if (customersLost == 0 && customersServed > 0) {
            bonus += Constants.BONUS_PERFECT_DAY;
        }
        // Bonus soddisfazione media
        if (avgSatisfaction >= 0.9f) bonus += Math.round(coinsEarned * 0.20f);
        else if (avgSatisfaction >= 0.7f) bonus += Math.round(coinsEarned * 0.10f);
        return Math.max(0, bonus);
    }

    // ---------------------------------------------------------------
    // Query
    // ---------------------------------------------------------------

    public DayPhase  getPhase()            { return phase;            }
    public float     getDayTimer()         { return dayTimer;         }
    public float     getDayDuration()      { return dayDuration;      }
    public float     getTimeRemaining()    { return dayDuration - dayTimer; }
    public boolean   isDayOver()           { return phase == DayPhase.DAY_END; }
    public boolean   isRushHour()          { return phase == DayPhase.RUSH_HOUR; }

    public float getProgressFraction() {
        return dayDuration > 0 ? dayTimer / dayDuration : 0f;
    }

    /** Moltiplicatore velocità spawn durante rush hour. */
    public float getSpawnMultiplier() {
        return isRushHour() ? 2.0f : 1.0f;
    }
}
