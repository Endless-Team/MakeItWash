package com.sushidays.entities;

public class Upgrade {

    public enum UpgradeType { FUNCTIONAL, COSMETIC }

    public final String      id;
    public final String      displayName;
    public final String      description;
    public final int         cost;
    public final UpgradeType type;
    public final int         unlockDay;  // giorno minimo per acquistare

    public Upgrade(String id, String displayName, String description,
                   int cost, UpgradeType type, int unlockDay) {
        this.id          = id;
        this.displayName = displayName;
        this.description = description;
        this.cost        = cost;
        this.type        = type;
        this.unlockDay   = unlockDay;
    }
}
