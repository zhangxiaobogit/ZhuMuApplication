package com.example.zhumuapplication.util;

public class SettingUtils {

    public static void setCameraId(int flag) {
        PreferencesUtils.putInt("CAMERA_ID", flag);

    }

    public static int getCameraId() {
        return PreferencesUtils.getInt("CAMERA_ID", 1);
    }

    /**
     * 活体检测的开关
     */

    public static void setLivenewssDetect(boolean flag) {
        PreferencesUtils.putBoolean("LIVENEWSSDETECT", flag);

    }

    public static boolean getLivenewssDetect() {
        return PreferencesUtils.getBoolean("LIVENEWSSDETECT", false);
    }

    public static void setSingleCompareScore(int threshold) {
        PreferencesUtils.putInt("SINGLECOMPARESCORE", threshold);
    }

    public static int getSingleCompareScore() {
        return PreferencesUtils.getInt("SINGLECOMPARESCORE", 60);
    }

    public static void setMoreCompareScore(int threshold) {
        PreferencesUtils.putInt("MORECOMPARESCORE", threshold);
    }

    public static int getMoreCompareScore() {
        return PreferencesUtils.getInt("MORECOMPARESCORE", 80);
    }
}
