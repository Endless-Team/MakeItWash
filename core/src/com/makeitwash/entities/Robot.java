package com.makeitwash.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Robot extends PlaceableEntity {
    private int targetX;
    private int targetY;
    private float speed;
    private float moveProgress;

    public Robot() {
        super();
        this.targetX = 0;
        this.targetY = 0;
        this.speed = 3f;
        this.moveProgress = 1f;
    }

    public Robot(int gridX, int gridY) {
        super();
        setGridPosition(gridX, gridY);
        this.targetX = gridX;
        this.targetY = gridY;
        this.speed = 3f;
        this.moveProgress = 1f;
    }

    @Override
    public void update(float delta) {
        if (moveProgress < 1f) {
            moveProgress += delta * speed * 0.5f;
            if (moveProgress >= 1f) {
                moveProgress = 1f;
                setGridPosition(targetX, targetY);
            } else {
                float startX = gridX * 64f;
                float startY = gridY * 64f;
                x = startX + (targetX * 64f - startX) * moveProgress;
                y = startY + (targetY * 64f - startY) * moveProgress;
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, x, y, 64, 64);
        }
    }

    public void moveTo(int newX, int newY) {
        if (moveProgress >= 1f) {
            targetX = newX;
            targetY = newY;
            moveProgress = 0f;
        }
    }

    public boolean isMoving() {
        return moveProgress < 1f;
    }
}
