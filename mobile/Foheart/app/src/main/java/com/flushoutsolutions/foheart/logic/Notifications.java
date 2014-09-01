package com.flushoutsolutions.foheart.logic;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;

import com.flushoutsolutions.foheart.application.FoHeart;

/**
 * Created by Manuel on 10/08/2014.
 */
public class Notifications {

    private static Notifications instance = null;

    public static Notifications get_instance()
    {
        if (instance==null)
            instance = new Notifications();

        return instance;
    }

    private Notifications() {

    }

    public static void playAlert(boolean sound, int vibration)
    {

        if (sound)
        {
            MediaPlayer player = MediaPlayer.create(FoHeart.getAppContext(), com.flushoutsolutions.foheart.R.raw.beep);
            player.start();
        }

        Vibrator v = (Vibrator) FoHeart.getAppContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(vibration);
    }
}
