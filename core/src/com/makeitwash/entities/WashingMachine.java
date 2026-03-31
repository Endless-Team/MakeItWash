package com.makeitwash.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class WashingMachine extends PlaceableEntity {
    private float washProgress;
    private boolean isWashing;
    private static final float WASH_DURATION = 3f;

    public WashingMachine() {
        super();
        this.washProgress = 0f;
        this.isWashing = false;
    }

    public WashingMachine(int gridX, int gridY) {
        super();
        setGridPosition(gridX, gridY);
        this.washProgress = 0f;
        this.isWashing = false;
    }

    @Override
    public void update(float delta) {
        if (isWashing) {
            washProgress += delta;
            if (washProgress >= WASH_DURATION) {
                washProgress = 0f;
                isWashing = false;
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, x, y, 64, 64);
        }
    }

    public void startWashing() {
        if (!isWashing) {
            isWashing = true;
            washProgress = 0f;
        }
    }

    public boolean isWashing() {
        return isWashing;
    }

    public float getWashProgress() {
        return washProgress / WASH_DURATION;
    }
}
