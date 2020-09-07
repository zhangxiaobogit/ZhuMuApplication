package com.example.zhumuapplication.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;


import java.util.Locale;

public class LanguageUtils {

    private static LanguageUtils soundUtils = new LanguageUtils();

    public static LanguageUtils getInstance() {
        return soundUtils;
    }

    public Locale getLanguage() {
        try {
            int language = 0;
            switch (language) {
                case 1:
                    return Locale.CHINA;
                case 2:
                    return Locale.TAIWAN;
                case 3:
                    return Locale.ENGLISH;
                case 4:
                    return Locale.ITALIAN;
                case 5:
                    return new Locale("ru");
                case 6:
                    return new Locale("es");
                case 7:
                    return new Locale("pt");
                case 8:
                    return Locale.JAPAN;
                default:
                    return Locale.getDefault();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Locale.getDefault();
    }

    public void initLanguage(Context context) {
        try {
            //根据读取到的数据，进行设置
            Resources resources = context.getResources();
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            Configuration configuration = resources.getConfiguration();
            configuration.setLocale(getLanguage());
            resources.updateConfiguration(configuration, displayMetrics);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
