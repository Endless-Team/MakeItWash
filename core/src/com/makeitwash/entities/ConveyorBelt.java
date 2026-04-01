package com.makeitwash.entities;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.makeitwash.world.Grid;

public class ConveyorBelt extends PlaceableEntity {
    private float speed;
    private float progress;
    
    private boolean connectedNorth = false;
    private boolean connectedSouth = false;
    private boolean connectedEast = false;
    private boolean connectedWest = false;
    
    private static Texture conveyorTexture;
    private static boolean texturesInitialized = false;
    
    private static final int TILE_SIZE = 64;

    private static void initTextures() {
        if (texturesInitialized) return;
        texturesInitialized = true;
        conveyorTexture = createConveyorTexture();
    }

    private static Texture createConveyorTexture() {
        Pixmap pixmap = new Pixmap(TILE_SIZE * 4, TILE_SIZE, Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        
        pixmap.setColor(0.32f, 0.35f, 0.4f, 1f);
        pixmap.fill();
        
        pixmap.setColor(0.5f, 0.52f, 0.55f, 1f);
        for (int x = 0; x < TILE_SIZE * 4; x += 16) {
            pixmap.fillRectangle(x, 8, 8, TILE_SIZE - 16);
        }
        
        Texture tex = new Texture(pixmap);
        tex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        pixmap.dispose();
        return tex;
    }

    public static void ensureTexturesLoaded() {
        initTextures();
    }

    public static void disposeTextures() {
        if (conveyorTexture != null) {
            conveyorTexture.dispose();
            conveyorTexture = null;
        }
        texturesInitialized = false;
    }

    public ConveyorBelt() {
        super();
        ensureTexturesLoaded();
        this.speed = 1f;
        this.progress = 0f;
    }

    public ConveyorBelt(boolean curved) {
        super();
        ensureTexturesLoaded();
        this.speed = 1f;
        this.progress = 0f;
    }

    public ConveyorBelt(int gridX, int gridY) {
        super();
        ensureTexturesLoaded();
        setGridPosition(gridX, gridY);
        this.speed = 1f;
        this.progress = 0f;
    }

    public void updateConnections(Grid grid) {
        connectedNorth = grid.hasConveyorAt(gridX, gridY + 1);
        connectedSouth = grid.hasConveyorAt(gridX, gridY - 1);
        connectedEast = grid.hasConveyorAt(gridX + 1, gridY);
        connectedWest = grid.hasConveyorAt(gridX - 1, gridY);
    }

    @Override
    public void update(float delta) {
        progress += delta * speed * 0.5f;
        if (progress > 1f) progress -= 1f;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (conveyorTexture == null) return;
        
        float rotation = getRotation();
        boolean isCurve = isCurveConnection();
        
        float cx = TILE_SIZE / 2f;
        float cy = TILE_SIZE / 2f;
        
        TextureRegion region = new TextureRegion(conveyorTexture);
        region.setRegionWidth(TILE_SIZE);
        region.setRegionHeight(TILE_SIZE);
        region.setRegionX((int)(progress * TILE_SIZE * 4));
        
        batch.draw(region, x, y, cx, cy, TILE_SIZE, TILE_SIZE, 1f, 1f, rotation);
    }

    private float getRotation() {
        int conn = countConnections();
        if (conn <= 1) return 0f;
        if (conn == 2) {
            if (connectedNorth && connectedSouth) return 90f;
            if (connectedEast && connectedWest) return 0f;
            if (connectedNorth && connectedEast) return 0f;
            if (connectedEast && connectedSouth) return 90f;
            if (connectedSouth && connectedWest) return 180f;
            if (connectedWest && connectedNorth) return 270f;
        }
        if (conn >= 3) {
            if (connectedNorth && connectedSouth) return 90f;
            return 0f;
        }
        return 0f;
    }

    private boolean isCurveConnection() {
        int conn = countConnections();
        if (conn != 2) return false;
        boolean straight = (connectedNorth && connectedSouth) || (connectedEast && connectedWest);
        return !straight;
    }

    private int countConnections() {
        int c = 0;
        if (connectedNorth) c++;
        if (connectedSouth) c++;
        if (connectedEast) c++;
        if (connectedWest) c++;
        return c;
    }

    public float getProgress() { return progress; }
    public void setSpeed(float speed) { this.speed = speed; }
    public boolean isConnectedNorth() { return connectedNorth; }
    public boolean isConnectedSouth() { return connectedSouth; }
    public boolean isConnectedEast() { return connectedEast; }
    public boolean isConnectedWest() { return connectedWest; }
}
