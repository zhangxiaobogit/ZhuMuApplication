package com.example.zhumuapplication.common;


import android.os.Environment;

public class Constants {
    public static final String ZHUMU_MODEL_FILE_DIRECTORY = Environment.getExternalStorageDirectory() + "/FaceModels/";
    public static final String ZHUMU_IDCARD_PHOTO_DIRECTORY = Environment.getExternalStorageDirectory() + "/ZhuMu/idcardphoto/";
    public static float faceScore = 0.35f;
    public static float pointScore = 0.4f;
    public static float clearness = 0.12f;

    public static final String APP_ID = "DvW1SzZP6wT7NxvkArtiGuU1wxHyoG74FCARFAtyPzYz";
    public static final String SDK_KEY = "HbdqsvPeMHPxnjSdWkt8Xy3vX6RuSyfpB23DY4Ki9aKp";
    /**
     * 识别阈值
     */
    public static final float SIMILAR_THRESHOLD = 0.8F;
    public static final int MAX_DETECT_NUM = 1;
    /**
     * 出错重试最大次数
     */
    public static final int MAX_RETRY_TIME = 3;

}
