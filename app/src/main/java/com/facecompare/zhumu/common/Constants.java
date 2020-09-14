package com.facecompare.zhumu.common;


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
    public static final int SOUNDPOOL_TYPE_SUCCESS = 1;//比对成功
    public static final int SOUNDPOOL_TYPE_FAIL = 2;//比对失败
    public static final int SOUNDPOOL_TYPE_DOOROPEN = 3;//门已开
    public static final int SOUNDPOOL_TYPE_PLSE_PUSH_CARD = 4;//请刷身份证
    public static final int SOUNDPOOL_TYPE_GET_CAR = 5;//车辆信息
    public static final int SOUNDPOOL_TYPE_WARNBEEP = 6;//警告提示
    public static final int SOUND_POOL_NOW_POWER = 7;//无权限人员
    public static final int SOUNDPOOL_TYPE_PUSH_CARD_DI = 8;//刷卡滴声
    public static final int SOUNDPOOL_TYPE_HOUSE_USER_CALL = 9;//业主呼叫声音
    public static final int SOUNDPOOL_TYPE_BIBIBI = 10;//BIBIBI

}
