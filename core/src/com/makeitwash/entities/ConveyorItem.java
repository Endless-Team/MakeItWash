package com.makeitwash.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class ConveyorItem {
    private float x, y;
    private float targetX, targetY;
    private float speed;
    private boolean moving = false;
    private Direction direction;
    
    private static Texture itemTexture;
    
    public enum Direction {
        NORTH(0, 1),
        SOUTH(0, -1),
        EAST(1, 0),
        WEST(-1, 0);
        
        public final int dx, dy;
        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }
    
    public ConveyorItem(float startX, float startY, Direction dir) {
        this.x = startX;
        this.y = startY;
        this.direction = dir;
        this.speed = 60f;
        
        if (itemTexture == null) {
            createItemTexture();
        }
    }
    
    private void createItemTexture() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(16, 16, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0.9f, 0.3f, 0.2f, 1f);
        pixmap.fillCircle(8, 8, 6);
        itemTexture = new Texture(pixmap);
        pixmap.dispose();
    }
    
    public void setTarget(float tx, float ty) {
        this.targetX = tx;
        this.targetY = ty;
        this.moving = true;
    }
    
    public void update(float delta) {
        if (!moving) return;
        
        float dx = targetX - x;
        float dy = targetY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (dist < 2f) {
            x = targetX;
            y = targetY;
            moving = false;
            return;
        }
        
        float move = speed * delta;
        if (move > dist) move = dist;
        
        x += (dx / dist) * move;
        y += (dy / dist) * move;
    }
    
    public void render(SpriteBatch batch) {
        if (itemTexture != null) {
            batch.draw(itemTexture, x - 8, y - 8, 16, 16);
        }
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isMoving() { return moving; }
    public Direction getDirection() { return direction; }
    
    public static void dispose() {
        if (itemTexture != null) {
            itemTexture.dispose();
            itemTexture = null;
        }
    }
}
