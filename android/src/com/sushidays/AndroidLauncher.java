package com.sushidays;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = true;   // per lo step SHAKE
        config.useCompass       = false;
        config.useGyroscope     = false;
        config.numSamples       = 2;      // anti-aliasing leggero

        initialize(new SushiDaysGame(), config);
    }
}
