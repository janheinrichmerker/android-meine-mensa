package com.heinrichreimer.meinemensa.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.heinrichreimer.meinemensa.annotations.Location;
import com.heinrichreimer.meinemensa.annotations.PriceCategory;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;

import java.util.HashSet;
import java.util.Set;

public class PreferencesUtils {

    private static final String KEY_PRICE_CATEGORY = "com.heinrichreimer.meinemensa.PRICE_CATEGORY";
    private static final String KEY_LOCATION = "com.heinrichreimer.meinemensa.LOCATION";
    private static final String KEY_LOCATIONS = "com.heinrichreimer.meinemensa.LOCATIONS";
    private static final String KEY_DATE_DIFF = "com.heinrichreimer.meinemensa.DATE_DIFF";
    private static final String KEY_VEGETARIAN_ONLY = "com.heinrichreimer.meinemensa.VEGETARIAN_ONLY";

    private PreferencesUtils() {
    }

    @PriceCategory
    public static int getPriceCategory(Context context) {
        //noinspection WrongConstant
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(KEY_PRICE_CATEGORY, PriceCategory.STUDENTS);
    }

    public static void setPriceCategory(Context context, @PriceCategory int category) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putInt(KEY_PRICE_CATEGORY, category)
                .apply();
    }

    @NonNull
    @Size(min = 1)
    private static Set<String> internalGetLocations(Context context) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Set<String> locationStrings = preferences.getStringSet(KEY_LOCATIONS, null);
        if (locationStrings == null) {
            //Probably old version so only one location is stored
            //Get old location and store as new set
            int location = preferences.getInt(KEY_LOCATION, Location.HARZMENSA);
            locationStrings = new HashSet<>(1);
            locationStrings.add(Integer.toString(location));
            preferences.edit()
                    .putStringSet(KEY_LOCATIONS, locationStrings)
                    .apply();
        }
        return locationStrings;
    }

    @Location
    @Size(min = 1)
    public static int[] getLocations(Context context) {
        Set<String> locationStrings = internalGetLocations(context);
        int[] locations = new int[locationStrings.size()];
        int i = 0;
        for (String locationString : locationStrings) {
            locations[i] = Integer.parseInt(locationString);
            i++;
        }
        //noinspection WrongConstant
        return locations;
    }


    public static boolean containsLocation(Context context, @Location int location) {
        int[] locations = getLocations(context);
        for (int test : locations) {
            if (test == location) {
                return true;
            }
        }
        return false;
    }

    public static void addLocation(Context context, @Location int location) {
        Set<String> locationStrings = internalGetLocations(context);
        locationStrings.add(Integer.toString(location));
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putStringSet(KEY_LOCATIONS, locationStrings)
                .apply();
    }

    public static boolean removeLocation(Context context, @Location int location) {
        Set<String> locationStrings = internalGetLocations(context);
        if (locationStrings.size() == 1) {
            return false;
        }
        locationStrings.remove(Integer.toString(location));
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putStringSet(KEY_LOCATIONS, locationStrings)
                .apply();
        return true;
    }

    @NonNull
    public static DateTime getDate(Context context) {
        DateTime now = DateTime.now();
        DateTime today = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), 0, 0);
        int days = PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_DATE_DIFF, 0);
        return today.plusDays(days);
    }

    @NonNull
    public static DateTime getNextWeekdayDate(Context context) {
        DateTime date = getDate(context);
        while (date.getDayOfWeek() == DateTimeConstants.SATURDAY ||
                date.getDayOfWeek() == DateTimeConstants.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    public static void setDate(Context context, DateTime date) {
        DateTime day = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 0, 0);
        DateTime now = DateTime.now();
        DateTime today = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), 0, 0);

        int days = Days.daysBetween(today.toLocalDate(), day.toLocalDate()).getDays();

        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putInt(KEY_DATE_DIFF, days)
                .apply();
    }

    public static boolean isVegetarianOnly(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(KEY_VEGETARIAN_ONLY, false);
    }

    public static void setVegetarianOnly(Context context, boolean vegetarianOnly) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(KEY_VEGETARIAN_ONLY, vegetarianOnly)
                .apply();
    }
}
