package com.example.musicbpm.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class to manage app preferences/settings.
 * Handles reading and writing of user preferences using SharedPreferences.
 */
public class PreferencesManager {

    private static final String PREFS_NAME = "MusicBPMPrefs";

    // Keys for preferences
    private static final String KEY_TAP_COUNT = "tap_count";
    private static final String KEY_RESET_TIMEOUT = "reset_timeout";
    private static final String KEY_BPM_DECIMAL = "bpm_decimal";
    private static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    private static final String KEY_THEME_MODE = "theme_mode";

    // Default values
    private static final int DEFAULT_TAP_COUNT = 4;
    private static final int DEFAULT_RESET_TIMEOUT = 3000; // 3 seconds
    private static final boolean DEFAULT_BPM_DECIMAL = false;
    private static final boolean DEFAULT_VIBRATION = true;
    private static final int DEFAULT_THEME_MODE = 0; // System default

    private SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Tap Count (number of taps used for calculation)
    public void setTapCount(int count) {
        prefs.edit().putInt(KEY_TAP_COUNT, count).apply();
    }

    public int getTapCount() {
        return prefs.getInt(KEY_TAP_COUNT, DEFAULT_TAP_COUNT);
    }

    // Reset Timeout (ms)
    public void setResetTimeout(int timeoutMs) {
        prefs.edit().putInt(KEY_RESET_TIMEOUT, timeoutMs).apply();
    }

    public int getResetTimeout() {
        return prefs.getInt(KEY_RESET_TIMEOUT, DEFAULT_RESET_TIMEOUT);
    }

    // BPM Decimal Display
    public void setBpmDecimalEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BPM_DECIMAL, enabled).apply();
    }

    public boolean isBpmDecimalEnabled() {
        return prefs.getBoolean(KEY_BPM_DECIMAL, DEFAULT_BPM_DECIMAL);
    }

    // Vibration
    public void setVibrationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply();
    }

    public boolean isVibrationEnabled() {
        return prefs.getBoolean(KEY_VIBRATION_ENABLED, DEFAULT_VIBRATION);
    }

    // Theme Mode (0: System, 1: Light, 2: Dark)
    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, DEFAULT_THEME_MODE);
    }

    // Clear all preferences
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}