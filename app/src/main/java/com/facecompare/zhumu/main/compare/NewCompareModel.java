package com.facecompare.zhumu.main.compare;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.facecompare.zhumu.main.ZhumuApplication;
import com.facecompare.zhumu.util.SettingUtils;
import com.facecompare.zhumu.util.camera.CameraHelper;
import com.facecompare.zhumu.view.ARCCameraListener;
import com.facecompare.zhumu.view.FaceRectView;

import java.util.concurrent.ConcurrentHashMap;

public class NewCompareModel {
    CameraHelper cameraHelper;
    private ARCCameraListener arcCameraListener;

    public void initCamera(AppCompatActivity activity, FaceRectView faceRectView,
                           View previewView, GetResultCallback getResultCallback) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        arcCameraListener = new ARCCameraListener(ZhumuApplication.getContext())//初始化cameralistener
                .setFaceRectView(faceRectView)
                .setPreview(previewView)
                .setRelultCallback(getResultCallback);
        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(previewView.getMeasuredWidth(), previewView.getMeasuredHeight()))
                .rotation(activity.getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(SettingUtils.getCameraId())
                .isMirror(SettingUtils.getCameraMirror())
                .previewOn(previewView)
                .cameraListener(arcCameraListener)
                .build();
        cameraHelper.init();
        cameraHelper.start();
    }

    private Bitmap idCardBitmap = null;

    public void setIdCardBitmap(Bitmap idPhoto) {
        idCardBitmap = idPhoto;
    }


    /**
     * 用于记录人脸识别相关状态
     */
    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();


    public void ARCRelese(AppCompatActivity activity) {
        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }

        arcCameraListener.ARCRelese(activity);
    }

    /**
     * 销毁引擎，faceHelper中可能会有特征提取耗时操作仍在执行，加锁防止crash
     */
    private void unInitEngine() {
        arcCameraListener.unInitEngine();
    }

    public void cameraStart() {
        if (cameraHelper != null) {
            cameraHelper.start();
        }
    }

    public void cameraStop() {
        if (cameraHelper != null) {
            cameraHelper.stop();
        }
    }
}
