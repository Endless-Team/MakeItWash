package com.makeitwash;

import com.badlogic.gdx.Game;
import com.makeitwash.screens.GameScreen;
import com.makeitwash.screens.MenuScreen;

public class MainGame extends Game {
    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }
}
