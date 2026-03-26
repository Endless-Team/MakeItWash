import com.badlogic.gdx.Game;
import screens.GameScreen;

public class MainGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen(this));
    }
}