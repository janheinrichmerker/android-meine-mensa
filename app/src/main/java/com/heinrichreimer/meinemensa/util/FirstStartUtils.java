package com.heinrichreimer.meinemensa.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class FirstStartUtils {
    private static final String KEY_FIRST_START = "com.heinrichreimer.meinemensa.FIRST_START";

    private FirstStartUtils() {
    }

    public static boolean isFirstStart(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.contains(KEY_FIRST_START) &&
                !preferences.getBoolean(KEY_FIRST_START, true)) {
            return false;
        } else {
            preferences.edit()
                    .putBoolean(KEY_FIRST_START, false)
                    .apply();
            return true;
        }
    }
}
