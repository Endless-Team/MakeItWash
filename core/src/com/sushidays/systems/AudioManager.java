package com.sushidays.systems;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.sushidays.utils.AssetLoader;

public class AudioManager {

    private static AudioManager instance;

    private float musicVolume = 0.7f;
    private float sfxVolume   = 0.8f;
    private Music currentMusic;

    private AudioManager() {}

    public static AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    public void setMusicVolume(float v) {
        musicVolume = v;
        if (currentMusic != null) currentMusic.setVolume(musicVolume);
    }

    public void setSfxVolume(float v) {
        sfxVolume = v;
    }

    public float getMusicVolume() { return musicVolume; }
    public float getSfxVolume()   { return sfxVolume;   }

    public void playMusic(Music music) {
        if (music == null) return;
        if (currentMusic == music && currentMusic.isPlaying()) return;
        if (currentMusic != null) currentMusic.stop();
        currentMusic = music;
        currentMusic.setLooping(true);
        currentMusic.setVolume(musicVolume);
        currentMusic.play();
    }

    public void playMenuMusic()  { playMusic(AssetLoader.getInstance().musicMenu); }
    public void playGameMusic()  { playMusic(AssetLoader.getInstance().musicGame); }
    public void playRushMusic()  { playMusic(AssetLoader.getInstance().musicRush); }

    public void stopMusic() {
        if (currentMusic != null) currentMusic.stop();
    }

    public void playSfx(Sound sound) {
        if (sound != null) sound.play(sfxVolume);
    }

    public void playCoin()    { playSfx(AssetLoader.getInstance().sfxCoin);    }
    public void playCut()     { playSfx(AssetLoader.getInstance().sfxCut);     }
    public void playBell()    { playSfx(AssetLoader.getInstance().sfxBell);    }
    public void playError()   { playSfx(AssetLoader.getInstance().sfxError);   }
    public void playSuccess() { playSfx(AssetLoader.getInstance().sfxSuccess); }
    public void playButton()  { playSfx(AssetLoader.getInstance().sfxButton);  }

    public void dispose() {
        stopMusic();
        instance = null;
    }
}
