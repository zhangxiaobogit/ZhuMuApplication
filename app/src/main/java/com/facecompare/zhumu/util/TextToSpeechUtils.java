package com.facecompare.zhumu.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class TextToSpeechUtils {

    private static TextToSpeechUtils instance = new TextToSpeechUtils();
    private TextToSpeech textToSpeech;//创建自带语音对象
    private boolean isStop;
    private boolean isInit;

    public static TextToSpeechUtils getInstance(){
        return instance;
    }

    public void init(Context context){
        try {
            textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = textToSpeech.setLanguage(LanguageUtils.getInstance().getLanguage());
                        if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
                                && result != TextToSpeech.LANG_AVAILABLE) {
                            isInit = false;
                            Log.e("zxb", "run->   初始化 语音initTTS   失败");
                        } else {
                            isInit = true;
                            Log.e("zxb", "run->   初始化 语音initTTS   成功");
                        }
                        textToSpeech.setPitch(1.0f);// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                        // 设置语速,默认1.0正常语速
                        textToSpeech.setSpeechRate(1.0f);
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public TextToSpeechUtils stop(){
        if (textToSpeech != null && textToSpeech.isSpeaking())
            textToSpeech.stop();
        isStop = true;
        return this;
    }

    public void speak(String msg){
        if (textToSpeech != null){
            if (isStop || !textToSpeech.isSpeaking()){
                textToSpeech.speak(msg,//输入中文，若不支持的设备则不会读出来
                        TextToSpeech.QUEUE_FLUSH,null,"");
                isStop = false;
            }
        }
    }

    public boolean isInit() {
        return isInit;
    }
}
