package com.app.zee.xifunpad.Storage;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    private static final String PREF_NAME = "MyAppPreferences";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Method to save a String value
    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    // Method to get a String value
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    // Method to save an int value
    public void saveInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    // Method to get an int value
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    // Method to save a float value
    public void saveFloat(String key, float value) {
        editor.putFloat(key, value);
        editor.apply();
    }

    // Method to get a float value
    public float getFloat(String key, float defaultValue) {
        return sharedPreferences.getFloat(key, defaultValue);
    }

    // Method to remove a value
    public void removeValue(String key) {
        editor.remove(key);
        editor.apply();
    }

    // Method to clear all preferences
    public void clearAll() {
        editor.clear();
        editor.apply();
    }
}

