package com.example.zhumuapplication.common;


import android.os.Environment;

public class Constants {
    public static final String ZHUMU_MODEL_FILE_DIRECTORY = Environment.getExternalStorageDirectory() + "/FaceModels/";
    public static final String ZHUMU_IDCARD_PHOTO_DIRECTORY = Environment.getExternalStorageDirectory() + "/ZhuMu/idcardphoto/";
    public static float faceScore = 0.35f;
    public static float pointScore = 0.4f;
    public static float clearness = 0.12f;
}
