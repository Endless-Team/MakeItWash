package com.makeitwash.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ConveyorBelt extends PlaceableEntity {
    private float speed;
    private float progress;
    private boolean curved;
    private static final float MAX_PROGRESS = 1f;

    public ConveyorBelt() {
        super();
        this.speed = 1f;
        this.progress = 0f;
        this.curved = false;
    }

    public ConveyorBelt(boolean curved) {
        super();
        this.speed = 1f;
        this.progress = 0f;
        this.curved = curved;
    }

    public ConveyorBelt(int gridX, int gridY) {
        super();
        setGridPosition(gridX, gridY);
        this.speed = 1f;
        this.progress = 0f;
        this.curved = false;
    }

    @Override
    public void update(float delta) {
        progress += delta * speed * 0.1f;
        if (progress > MAX_PROGRESS) {
            progress = 0f;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, x, y, 64, 64);
        }
    }

    public float getProgress() {
        return progress;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
