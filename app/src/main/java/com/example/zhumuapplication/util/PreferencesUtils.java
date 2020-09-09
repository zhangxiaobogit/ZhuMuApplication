package com.example.zhumuapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.zhumuapplication.main.ZhumuApplication;

import java.util.Map;

public class PreferencesUtils {

    public static String PREFERENCE_NAME = "LocalInfo";

    public static boolean putString(String key, String value) {
        SharedPreferences settings = ZhumuApplication.getContextObject().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        return editor.commit();
    }


    public static String getString(String key, String defaultValue) {
        SharedPreferences settings = ZhumuApplication.getContextObject().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return settings.getString(key, defaultValue);
    }

    public static boolean putInt(String key, int value) {
        SharedPreferences settings = ZhumuApplication.getContextObject().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        return editor.commit();
    }


    public static int getInt(String key, int defaultValue) {
        SharedPreferences settings = ZhumuApplication.getContextObject().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return settings.getInt(key, defaultValue);
    }

    public static boolean putLong(Context context, String key, long value) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        return editor.commit();
    }

    public static long getLong(Context context, String key) {
        return getLong(context, key, -1);
    }

    public static long getLong(Context context, String key, long defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return settings.getLong(key, defaultValue);
    }

    public static boolean putFloat(String key, float value) {
        SharedPreferences settings = ZhumuApplication.getContextObject().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        return editor.commit();
    }

    public static float getFloat(String key) {
        return getFloat(key, -1);
    }

    public static float getFloat(String key, float defaultValue) {
        SharedPreferences settings = ZhumuApplication.getContextObject().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return settings.getFloat(key, defaultValue);
    }

    public static boolean putBoolean(String key, boolean value) {
        SharedPreferences settings = ZhumuApplication.getContextObject().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }


    public static boolean getBoolean(String key, boolean defaultValue) {
        SharedPreferences settings = ZhumuApplication.getContextObject().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean(key, defaultValue);
    }

    public static void cleanSp(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        Map<String, ?> map = settings.getAll();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            if (!entry.getKey().equals("tmh")) {
                editor.remove(entry.getKey());
            }
        }
        editor.commit();
    }
}