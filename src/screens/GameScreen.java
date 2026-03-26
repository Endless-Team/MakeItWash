package screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.makeitwash.MainGame;
import world.World;

public class GameScreen extends ScreenAdapter {
    private final MainGame game;
    private final World world;

    public GameScreen(MainGame game) {
        this.game = game;
        this.world = new World();
    }

    @Override
    public void render(float delta) {
        world.update(delta);

        Gdx.gl.glClearColor(0.12f, 0.14f, 0.16f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        world.render();
    }

    @Override
    public void dispose() {
        world.dispose();
    }
}
