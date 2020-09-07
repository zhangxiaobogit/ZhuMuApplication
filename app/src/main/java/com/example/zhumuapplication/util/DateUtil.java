package com.example.zhumuapplication.util;

import android.content.Context;
import android.provider.Settings;

public class DateUtil {
    public static String getDateFormate(Context context) {
        return Settings.System.getString(context.getContentResolver(),
                Settings.System.DATE_FORMAT);
    }
}
