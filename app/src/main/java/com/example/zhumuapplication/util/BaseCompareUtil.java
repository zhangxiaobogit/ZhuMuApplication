package com.example.zhumuapplication.util;

import android.graphics.Rect;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.cshtface.sdk.FaceTools;
import com.cshtface.sdk.bean.msg;
import com.example.zhumuapplication.common.Constants;
import com.example.zhumuapplication.mian.ZhumuApplication;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static com.cshtface.common.Constants.IMG_ANGLE_0;
import static com.cshtface.common.Constants.IMG_FORMAT_BINARY;
import static com.cshtface.common.Constants.IMG_MIRROR_NONE;
import static com.cshtface.common.Constants.OP_ALIGN;
import static com.cshtface.common.Constants.OP_DET;
import static com.cshtface.common.Constants.OP_QUALITY_BASE;

/**
 * Created by Administrator on 2019-10-15.
 */

public class BaseCompareUtil {

    public static BaseCompareUtil instance = null;
    private FaceTools faceTools;//人脸比对，提取特征值工具
    private boolean isInit;

    private BaseCompareUtil() {

    }

    public interface CallBack {
        void onInitStart();

        void onInitSuccess();

        void onInitFail();
    }

    public void init(final CallBack cb) {
        if (isInit) {
            cb.onInitSuccess();
        } else {
            Executors.newCachedThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    faceToolInit(cb);
                }
            });
        }
    }

    public boolean isInit() {
        return isInit;
    }


    private void faceToolInit(final CallBack cb) {
        if (faceTools == null) {
            cb.onInitStart();
            faceTools = new FaceTools();
            faceTools.setModelPath(Constants.ZHUMU_MODEL_FILE_DIRECTORY);
            boolean isInitHandle = faceTools.initAllTools(30, 700);
            boolean isInitSuccess = faceTools.initFeatureHandle();
            Log.e("zxb", "run->  初始化faceTool 检测featureHandler状态:" + isInitHandle + "=" + isInitSuccess);
            if (isInitHandle && isInitSuccess) {
                Log.e("zxb", "run->  faceTool  ：初始化成功");
                isInit = true;
                cb.onInitSuccess();
            } else {
                isInit = false;
                cb.onInitFail();
                Log.e("zxb", "run->  faceTool  ：初始化失败");
                faceTools = null;
                SystemClock.sleep(5000);
                Log.e("zxb", "run->  faceTool  ：正在重新解压模型文件");
                String path = Constants.ZHUMU_MODEL_FILE_DIRECTORY;
                FileUtil.deleteDir(path);
                File file = new File(path);
                UnzipAssets zip = new UnzipAssets();
                if (!file.exists() || (file.listFiles() != null && file.listFiles().length < 7)) {
                    try {
                        zip.unZip(ZhumuApplication.getContext(), "FaceModels.zip",
                                Environment.getExternalStorageDirectory().getPath());
                    } catch (IOException e) {
                        Log.e("zxb", "run->  faceTool  ：解压模型文件失败");
                        e.printStackTrace();
                    }
                }
                faceToolInit(cb);
            }
        }
    }


    public synchronized static BaseCompareUtil getInstance() {
        if (instance == null) {
            instance = new BaseCompareUtil();
        }
        return instance;
    }

    /**
     * 提取人脸特征值
     *
     * @param idCardFaces 检测出的人脸数据
     * @param isCompared  是否比对对象
     * @return 人脸特征值
     */
    public byte[] getFaceDataFeature(msg[] idCardFaces, boolean isCompared) {
        if (faceTools != null && idCardFaces != null) {
            return faceTools.getFeature(idCardFaces[0], isCompared);
        }
        return null;
    }

    /**
     * 提取人脸特征值
     *
     * @param isCompared 是否比对对象
     * @return 人脸特征值
     */
    public byte[] getFaceDataFeature(byte[] nowfaceBmp, int width, int height, int angle, boolean isCompared) {
        if (faceTools != null) {
            msg[] idCardFaces = getCheckFaceMSG(nowfaceBmp, width, height, angle);
            if (idCardFaces != null)
                return faceTools.getFeature(idCardFaces[0], isCompared);
        }
        return null;
    }


    /**
     * 单人比对
     *
     * @param nowFacefeature    现场特征值
     * @param idCardFacefeature 被比对对象
     * @return 分数集合
     */
    public float[] getNNCompareScore(byte[] nowFacefeature, byte[] idCardFacefeature) {
        if (faceTools != null && nowFacefeature != null && idCardFacefeature != null) {
            return faceTools.compare(nowFacefeature, idCardFacefeature);
        }
        return null;
    }

    /**
     * 获取人脸的参数()
     *
     * @param idCardFaceData
     * @param rect
     * @return
     */
    public msg[] getCheckFaceMSG(byte[] idCardFaceData, Rect rect) {//一般针对与身份证
        if (faceTools != null && idCardFaceData != null) {
            return faceTools.checkImage(idCardFaceData, OP_ALIGN | OP_QUALITY_BASE | OP_DET, IMG_FORMAT_BINARY,
                    rect.width(), rect.height(), IMG_ANGLE_0, IMG_MIRROR_NONE);
        }
        return null;
    }

    public msg[] getCheckFaceMSG(byte[] nowfaceBmp, Rect rect, int angle) {//一般针对于现场照
        if (faceTools != null && nowfaceBmp != null) {
            return faceTools.checkImage(nowfaceBmp, OP_ALIGN | OP_QUALITY_BASE | OP_DET, IMG_FORMAT_BINARY,
                    rect.width(), rect.height(), angle, IMG_MIRROR_NONE);
        }
        return null;
    }

    public msg[] getCheckFaceMSG(byte[] nowfaceBmp, int width, int height, int angle) {//一般针对于现场照
        if (faceTools != null && nowfaceBmp != null) {
            return faceTools.checkImage(nowfaceBmp, OP_ALIGN | OP_QUALITY_BASE | OP_DET, IMG_FORMAT_BINARY,
                    width, height, angle, IMG_MIRROR_NONE);
        }
        return null;
    }

    public msg[] getCheckFaceMSG(byte[] idCardFaceData) {//一般针对于身份证
        if (faceTools != null && idCardFaceData != null) {
            return faceTools.checkImage(idCardFaceData, OP_ALIGN | OP_QUALITY_BASE | OP_DET, IMG_FORMAT_BINARY,
                    0, 0, IMG_ANGLE_0, IMG_MIRROR_NONE);
        }
        return null;
    }

    public byte[] getFeatureFromData(byte[] imgData, boolean isCompared) {
        try {
            if (faceTools != null) {
                msg[] data = getCheckFaceMSG(imgData);
                if (null != data && null != data[0])
                    return faceTools.getFeature(data[0], isCompared);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void faceToolsDestory() {
        if (faceTools != null) {
            faceTools.destroy();
        }
    }








}
