package com.makeitwash.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Customer extends PlaceableEntity {
    private int satisfaction;
    private float waitTime;
    private static final float MAX_WAIT = 30f;

    public Customer(int gridX, int gridY) {
        super();
        setGridPosition(gridX, gridY);
        this.satisfaction = 100;
        this.waitTime = 0f;
    }

    @Override
    public void update(float delta) {
        waitTime += delta;
        if (waitTime > MAX_WAIT * 0.5f) {
            satisfaction -= delta * 2f;
        }
        if (satisfaction < 0) satisfaction = 0;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, x, y, 64, 64);
        }
    }

    public int getSatisfaction() {
        return (int)satisfaction;
    }

    public boolean isLeaving() {
        return satisfaction <= 0;
    }
}
