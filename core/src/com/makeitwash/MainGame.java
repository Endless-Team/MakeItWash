package com.makeitwash;

import com.badlogic.gdx.Game;
import com.makeitwash.screens.GameScreen;

public class MainGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen(this));
    }
}
