package com.makeitwash.entities;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ConveyorItem {

    private float x, y;
    private float targetX, targetY;
    private float speed;
    private boolean moving = false;
    private Direction direction;

    // FIX #2: texture statica con reference counting per dispose sicuro
    private static Texture itemTexture;
    private static int instanceCount = 0;

    public enum Direction {
        NORTH(0, 1), SOUTH(0, -1), EAST(1, 0), WEST(-1, 0);
        public final int dx, dy;
        Direction(int dx, int dy) { this.dx = dx; this.dy = dy; }
    }

    public ConveyorItem(float startX, float startY, Direction dir) {
        this.x         = startX;
        this.y         = startY;
        this.direction = dir;
        this.speed     = 60f;
        instanceCount++;
        // FIX #2: creiamo la texture solo se non esiste (evita overwrite senza dispose)
        if (itemTexture == null) {
            itemTexture = createItemTexture();
        }
    }

    // FIX #2: metodo statico coerente con il campo statico
    private static Texture createItemTexture() {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.9f, 0.3f, 0.2f, 1f);
        pixmap.fillCircle(8, 8, 6);
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    /** Imposta target aggiornando anche la direction (FIX #7) */
    public void setTarget(float tx, float ty, Direction newDirection) {
        this.targetX   = tx;
        this.targetY   = ty;
        this.direction = newDirection;
        this.moving    = true;
    }

    /** Overload retrocompatibile senza cambio direction */
    public void setTarget(float tx, float ty) {
        this.targetX = tx;
        this.targetY = ty;
        this.moving  = true;
    }

    public void update(float delta) {
        if (!moving) return;
        float dx   = targetX - x;
        float dy   = targetY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < 2f) {
            x = targetX; y = targetY; moving = false;
            return;
        }
        float move = Math.min(speed * delta, dist);
        x += (dx / dist) * move;
        y += (dy / dist) * move;
    }

    public void render(SpriteBatch batch) {
        if (itemTexture != null) {
            batch.draw(itemTexture, x - 8, y - 8, 16, 16);
        }
    }

    /** Chiamare quando l'item viene rimosso dalla griglia (FIX #2) */
    public void onDestroy() {
        instanceCount--;
        if (instanceCount <= 0) {
            dispose();
            instanceCount = 0;
        }
    }

    public float     getX()         { return x; }
    public float     getY()         { return y; }
    public boolean   isMoving()     { return moving; }
    public Direction getDirection() { return direction; }

    public static void dispose() {
        if (itemTexture != null) {
            itemTexture.dispose();
            itemTexture = null;
        }
    }
}