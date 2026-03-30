package com.makeitwash.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player extends PlaceableEntity {
    private float speed;
    private float dirX;
    private float dirY;

    public Player(int gridX, int gridY) {
        super();
        setGridPosition(gridX, gridY);
        this.speed = 200f;
        this.dirX = 0;
        this.dirY = 0;
    }

    @Override
    public void update(float delta) {
        x += dirX * speed * delta;
        y += dirY * speed * delta;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, x, y, 64, 64);
        }
    }

    public void move(float dx, float dy) {
        this.dirX = dx;
        this.dirY = dy;
    }
}
