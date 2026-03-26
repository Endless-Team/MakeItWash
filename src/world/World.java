package world;

import entities.Robot;
import entities.ConveyorBelt;
import java.util.ArrayList;
import java.util.List;

public class World {
    private final List<Robot> robots = new ArrayList<>();
    private final List<ConveyorBelt> belts = new ArrayList<>();

    public World() {
        robots.add(new Robot(2, 2));
        belts.add(new ConveyorBelt(4, 2));
    }

    public void update(float delta) {
        for (Robot robot : robots) {
            robot.update(delta);
        }
        for (ConveyorBelt belt : belts) {
            belt.update(delta);
        }
    }

    public void render() {
        // rendering base del mondo
    }

    public void dispose() {
        // cleanup
    }
}
