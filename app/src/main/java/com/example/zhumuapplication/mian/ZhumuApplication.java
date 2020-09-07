package com.example.zhumuapplication.mian;

import android.app.Application;
import android.content.Context;

import com.csht.cshthardwarecontrol.api.UtilsConfig;
import com.example.zhumuapplication.common.Constants;
import com.example.zhumuapplication.util.BaseCompareUtil;
import com.example.zhumuapplication.util.TextToSpeechUtils;

import java.io.File;

public class ZhumuApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        initDir();
        TextToSpeechUtils.getInstance().init(this);
        UtilsConfig.getInstance().init(this);
        BaseCompareUtil.getInstance().init(null);
    }

    public static Context getContext() {
        return context;
    }

    public void initDir() {
        File file = new File(Constants.ZHUMU_MODEL_FILE_DIRECTORY);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
}
