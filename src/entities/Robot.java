package entities;

public class Robot {
    private int gridX;
    private int gridY;

    public Robot(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public void update(float delta) {
        // logica robot: movimento, pickup, delivery
    }
}
