package com.example.zhumuapplication.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import com.csht.common.LogUtils;
import com.example.zhumuapplication.R;
import com.example.zhumuapplication.common.Constants;
import com.example.zhumuapplication.main.ZhumuApplication;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2019-2-19.
 */

public class SoundUtils {
    private static SoundUtils instance = null;

    private SoundUtils() {
        getSoundPool();
    }

    public static SoundUtils getInstance() {
        if (instance == null) {
            instance = new SoundUtils();
        }
        return instance;
    }

    /**
     * 播放警告音
     *
     * @param handler
     */
    public void WarnResult(final Handler handler) {
        final WarnSoundPlay warnSoundPlay = new WarnSoundPlay(handler);
        handler.post(warnSoundPlay);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(warnSoundPlay);
            }
        }, 7 * 1000);
    }

    class WarnSoundPlay implements Runnable {
        Handler handler;

        public WarnSoundPlay(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            soundPoolPlay(Constants.SOUNDPOOL_TYPE_WARNBEEP);
            handler.postDelayed(this, 600);
        }
    }

    /**
     * 播放自定义声音
     *
     * @param conpareFlag
     * @param name
     */
    ExecutorService comparePlaySoundExecutor;

    public void playSelfCompareSound(final boolean conpareFlag, final String name) {
        if (comparePlaySoundExecutor == null) {
            comparePlaySoundExecutor = Executors.newCachedThreadPool();
        }
        comparePlaySoundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String sound;
                if (TextToSpeechUtils.getInstance().isInit()) {//自定义语音开关
//                    if (true) {//播放姓名
//                        if (conpareFlag) {
//                            if (!TextUtils.isEmpty(name))
//                                sound = name;
//                            else
//                                sound = "验证成功";
//                        } else {
//                            sound = "验证失败";
//                        }
//                    } else {//播放自定义内容
                        if (conpareFlag) {
                            if (!TextUtils.isEmpty("")) {
                                sound = "自定义声音";
                            } else {
                                sound = "验证成功";
                            }
                        } else {
                            if (!TextUtils.isEmpty("")) {
                                sound = "自定义声音";
                            } else {
                                sound = "验证失败";
                            }
                        }
//                    }
                } else {
                    if (conpareFlag) {
                        sound = "验证成功";
                    } else {
                        sound = "验证失败";
                    }
                }
                tempToSpeech(sound);
            }
        });
    }

    private void tempToSpeech(final String msg) {
        if ("验证成功".equals(msg)) {
            soundPoolPlay(Constants.SOUNDPOOL_TYPE_SUCCESS);
        } else if ("验证失败".equals(msg)) {
            soundPoolPlay(Constants.SOUNDPOOL_TYPE_FAIL);
        } else {
            TextToSpeechUtils.getInstance().stop().speak(msg);
        }
    }

    public HashMap<Integer, Integer> musicMap;
    public SoundPool soundPool;
    int playCall;

    private void getSoundPool() {
        if (soundPool != null && musicMap != null) return;
        musicMap = new HashMap<>();
        soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        musicMap.put(Constants.SOUNDPOOL_TYPE_SUCCESS, soundPool.load(ZhumuApplication.getContext(), R.raw.face_check_suc, 1));
        musicMap.put(Constants.SOUNDPOOL_TYPE_FAIL, soundPool.load(ZhumuApplication.getContext(), R.raw.face_check_fail, 1));
        musicMap.put(Constants.SOUNDPOOL_TYPE_DOOROPEN, soundPool.load(ZhumuApplication.getContext(), R.raw.voice_door, 1));
        musicMap.put(Constants.SOUNDPOOL_TYPE_PLSE_PUSH_CARD, soundPool.load(ZhumuApplication.getContext(), R.raw.voice_idcard, 1));
        musicMap.put(Constants.SOUNDPOOL_TYPE_GET_CAR, soundPool.load(ZhumuApplication.getContext(), R.raw.voice_getcarinfo, 1));
        musicMap.put(Constants.SOUNDPOOL_TYPE_WARNBEEP, soundPool.load(ZhumuApplication.getContext(), R.raw.warnbeep, 1));//逃犯警示音
        musicMap.put(Constants.SOUND_POOL_NOW_POWER, soundPool.load(ZhumuApplication.getContext(), R.raw.no_power, 1));
        musicMap.put(Constants.SOUNDPOOL_TYPE_PUSH_CARD_DI, soundPool.load(ZhumuApplication.getContext(), R.raw.success, 1));//读卡提示音
        musicMap.put(Constants.SOUNDPOOL_TYPE_BIBIBI, soundPool.load(ZhumuApplication.getContext(), R.raw.bibibi, 1));//读卡提示音

    }

    /**
     * 播放soundool声音
     *
     * @param musicId
     */
    public void soundPoolPlay(int musicId) {
        soundPool.play(musicMap.get(musicId), 1, 1, 0, 0, 1);
    }

    public void soundPoolLoopPlay(int musicId) {
        playCall = soundPool.play(musicMap.get(musicId), 1, 1, 1, -1, 1);
    }

    public void soundPoolLoopStopPlayCall() {
        soundPool.stop(playCall);
    }


    /**
     * 播放自定义内容
     *
     * @param soundContext
     */
    public void playSoundContent(String soundContext, Context context) {
        if (TextToSpeechUtils.getInstance().isInit() && !TextUtils.isEmpty(soundContext)) {//自定义语音开关
            LogUtils.e("zxb", "******" + soundContext);
            TextToSpeechUtils.getInstance().speak(soundContext);//输入中文，若不支持的设备则不会读出来
        } else {
            Toast.makeText(context, "暂不支持语音播报", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 播放票务信息
     *
     * @param ticketResultBean
     * @param currentActivity
     */
}