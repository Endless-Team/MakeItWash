package com.makeitwash.items;

public class CleanClothing extends Item {
    public static final float BASE_VALUE = 25f;

    public CleanClothing() {
        super("Vestito Pulito", BASE_VALUE);
        this.isDirty = false;
        this.isWashed = true;
        this.isProcessed = true;
    }
}
