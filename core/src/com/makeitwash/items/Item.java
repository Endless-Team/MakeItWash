package com.makeitwash.items;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Item {
    protected String name;
    protected float value;
    protected boolean isDirty;
    protected boolean isWashed;
    protected boolean isProcessed;
    protected TextureRegion texture;

    public Item(String name, float value) {
        this.name = name;
        this.value = value;
        this.isDirty = true;
        this.isWashed = false;
        this.isProcessed = false;
    }

    public void wash() {
        if (isDirty) {
            isDirty = false;
            isWashed = true;
        }
    }

    public void process() {
        if (isWashed) {
            isProcessed = true;
        }
    }

    public String getName() {
        return name;
    }

    public float getValue() {
        return value;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public boolean isWashed() {
        return isWashed;
    }

    public boolean isProcessed() {
        return isProcessed;
    }
}
