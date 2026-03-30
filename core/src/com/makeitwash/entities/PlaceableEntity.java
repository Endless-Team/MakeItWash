package com.makeitwash.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class PlaceableEntity {
    protected int gridX;
    protected int gridY;
    protected float x;
    protected float y;
    protected TextureRegion texture;
    protected boolean active;

    public PlaceableEntity() {
        this.active = true;
    }

    public abstract void update(float delta);

    public abstract void render(SpriteBatch batch);

    public void setGridPosition(int x, int y) {
        this.gridX = x;
        this.gridY = y;
        this.x = x * 64f;
        this.y = y * 64f;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
