package com.thunsaker.soup;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/*
 * Created by @thunsaker
 */
public class PreferencesHelper {
    public final static String PREFS_NAME = "SoupPrefs";

    // Foursquare Prefs
    public static boolean getFoursquareConnected(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(
                context.getString(R.string.prefs_foursquare_connected),
                false);
    }
    public static void setFoursquareConnected(Context context, boolean newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(
                context.getString(R.string.prefs_foursquare_connected),
                newValue);
        prefsEditor.apply();
    }

    public static String getFoursquareToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(
                context.getString(R.string.prefs_foursquare_token),
                null);
    }
    public static void setFoursquareToken(Context context, String newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putString(
                context.getString(R.string.prefs_foursquare_token),
                newValue);
        prefsEditor.apply();
    }

    public static String getFoursquareApiKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(
                context.getString(R.string.prefs_foursquare_apikey),
                null);
    }
    public static void setFoursquareApiKey(Context context, String newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putString(
                context.getString(R.string.prefs_foursquare_apikey),
                newValue);
        prefsEditor.apply();
    }

    public static String getFoursquareUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(
                context.getString(R.string.prefs_foursquare_user_id),
                null);
    }
    public static void setFoursquareUserId(Context context, String newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putString(
                context.getString(R.string.prefs_foursquare_user_id),
                newValue);
        prefsEditor.apply();
    }

    public static int getFoursquareSuperuserLevel(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getInt(
                context.getString(R.string.prefs_foursquare_superuser_level),
                0);
    }
    public static void setFoursquareSuperuserLevel(Context context, int newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putInt(
                context.getString(R.string.prefs_foursquare_superuser_level),
                newValue);
        prefsEditor.apply();
    }

    public static boolean getShownSearchOverlay(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(
                context.getString(R.string.prefs_foursquare_search_overlay),
                false);
    }
    public static void setShownSearchOverlay(Context context, boolean newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(
                context.getString(R.string.prefs_foursquare_search_overlay),
                newValue);
        prefsEditor.apply();
    }

    public static boolean getShownNavDrawer(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(
                context.getString(R.string.prefs_soup_nav_drawer),
                false);
    }
    public static void setShownNavDrawer(Context context, boolean newValue) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(
                context.getString(R.string.prefs_soup_nav_drawer),
                newValue);
        prefsEditor.apply();
    }

    public static void migrateSuperUserPref(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Editor prefsEditor = prefs.edit();
        String superUserKey = context.getString(R.string.prefs_foursquare_superuser_level);
        String previousValueString = prefs.getString(superUserKey, "-1");
        int previousValueInt = !previousValueString.equals("") ? Integer.parseInt(previousValueString) : -1;
        prefsEditor.remove(superUserKey);
        prefsEditor.putInt(superUserKey, previousValueInt);
        prefsEditor.commit();
    }
}