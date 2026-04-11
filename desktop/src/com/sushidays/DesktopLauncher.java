package com.sushidays;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SushiDays");
        config.setWindowedMode(1280, 720);
        config.setResizable(true);
        config.setForegroundFPS(60);
        config.useVsync(true);
        new Lwjgl3Application(new SushiDaysGame(), config);
    }
}