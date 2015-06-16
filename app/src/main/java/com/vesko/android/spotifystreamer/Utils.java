package com.vesko.android.spotifystreamer;

public class Utils {

    public static String getTimeString(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        return minutes + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }
}
