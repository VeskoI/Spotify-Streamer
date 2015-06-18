package com.vesko.android.spotifystreamer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utils {

    public static String getTimeString(int millis) {
        int seconds = millis / 1000;
        int minutes = seconds / 60;
        return minutes + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }

    public static String getCountryCode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_country_code_key),
                context.getString(R.string.pref_country_code_default_value));
    }

    public static boolean lockscreenNotificationEnalbed(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_enable_lockscreen_controls_key), false);
    }
}
