package com.makeitwash.world;

public class GameSettings {

    private static final GameSettings instance = new GameSettings();

    private float masterVolume   = 0.8f;
    private float musicVolume    = 0.7f;
    private float sfxVolume      = 0.85f;
    private float uiScale        = 1.0f;

    private boolean fullscreen       = false;
    private boolean showGrid         = true;
    private boolean pauseOnFocusLost = true;
    private boolean showHints        = true;
    private boolean lowPowerMode     = false;

    private GameSettings() {}

    public static GameSettings get() { return instance; }

    public float getMasterVolume()   { return masterVolume; }
    public float getMusicVolume()    { return musicVolume; }
    public float getSfxVolume()      { return sfxVolume; }
    public float getUiScale()        { return uiScale; }
    public boolean isFullscreen()    { return fullscreen; }
    public boolean isShowGrid()      { return showGrid; }
    public boolean isPauseOnFocusLost() { return pauseOnFocusLost; }
    public boolean isShowHints()     { return showHints; }
    public boolean isLowPowerMode()  { return lowPowerMode; }

    public void setMasterVolume(float v)      { masterVolume = v; }
    public void setMusicVolume(float v)       { musicVolume = v; }
    public void setSfxVolume(float v)         { sfxVolume = v; }
    public void setUiScale(float v)           { uiScale = v; }
    public void setFullscreen(boolean v)      { fullscreen = v; }
    public void setShowGrid(boolean v)        { showGrid = v; }
    public void setPauseOnFocusLost(boolean v){ pauseOnFocusLost = v; }
    public void setShowHints(boolean v)       { showHints = v; }
    public void setLowPowerMode(boolean v)    { lowPowerMode = v; }

    public void reset() {
        masterVolume   = 0.8f;
        musicVolume    = 0.7f;
        sfxVolume      = 0.85f;
        uiScale        = 1.0f;
        fullscreen     = false;
        showGrid       = true;
        pauseOnFocusLost = true;
        showHints      = true;
        lowPowerMode   = false;
    }
}
