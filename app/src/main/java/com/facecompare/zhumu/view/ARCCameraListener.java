package com.facecompare.zhumu.view;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.facecompare.zhumu.R;
import com.facecompare.zhumu.common.Constants;
import com.facecompare.zhumu.faceserver.FaceServer;
import com.facecompare.zhumu.main.compare.GetResultCallback;
import com.facecompare.zhumu.model.DrawInfo;
import com.facecompare.zhumu.model.FacePreviewInfo;
import com.facecompare.zhumu.model.RequestFeatureStatus;
import com.facecompare.zhumu.model.RequestLivenessStatus;
import com.facecompare.zhumu.util.ConfigUtil;
import com.facecompare.zhumu.util.DrawHelper;
import com.facecompare.zhumu.util.SettingUtils;
import com.facecompare.zhumu.util.ZhumuToastUtil;
import com.facecompare.zhumu.util.camera.CameraListener;
import com.facecompare.zhumu.util.camera.RecognizeColor;
import com.facecompare.zhumu.util.face.FaceHelper;
import com.facecompare.zhumu.util.face.LivenessType;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 描 述：获取视频流进行比对
 * 作 者：zxb  2020-09-23 9:28
 * 修改描述： XXX
 * 修 改 人： XXX  2020-09-23 9:28
 * 修改版本： XXX
 */
public class ARCCameraListener implements CameraListener {
    private Camera.Size previewSize;
    private View previewView;
    private DrawHelper drawHelper;//画框处理
    private FaceHelper faceHelper;//人脸处理
    private Context mContext;
    ARCFaceListener arcFaceListener;

    public ARCCameraListener(Context context) {
        mContext = context;
        arcFaceListener = new ARCFaceListener();
        initEngine(context);
    }

    public ARCCameraListener setPreview(View preview) {
        this.previewView = preview;
        return this;

    }

    private int ftInitCode = -1;
    private int frInitCode = -1;
    private int flInitCode = -1;
    /**
     * VIDEO模式人脸检测引擎，用于预览帧人脸追踪
     */
    private FaceEngine ftEngine;
    /**
     * 用于特征提取的引擎
     */
    private FaceEngine frEngine;
    /**
     * IMAGE模式活体检测引擎，用于预览帧人脸活体检测
     */
    private FaceEngine flEngine;

    public void initEngine(Context context) {
        ftEngine = new FaceEngine();
        ftInitCode = ftEngine.init(context, DetectMode.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(context),
                16, Constants.MAX_DETECT_NUM, FaceEngine.ASF_FACE_DETECT);

        frEngine = new FaceEngine();
        frInitCode = frEngine.init(context, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                16, Constants.MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION);

        flEngine = new FaceEngine();
        flInitCode = flEngine.init(context, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                16, Constants.MAX_DETECT_NUM, FaceEngine.ASF_LIVENESS);

        Log.e("zxb", "initEngine:  init: " + ftInitCode);

        if (ftInitCode != ErrorInfo.MOK) {
            String error = context.getString(R.string.specific_engine_init_failed, "ftEngine", ftInitCode);
            Log.e("zxb", "initEngine: " + error);
            ZhumuToastUtil.showToast(error);
        }
        if (frInitCode != ErrorInfo.MOK) {
            String error = context.getString(R.string.specific_engine_init_failed, "frEngine", frInitCode);
            Log.e("zxb", "initEngine: " + error);
            ZhumuToastUtil.showToast(error);
        }
        if (flInitCode != ErrorInfo.MOK) {
            String error = context.getString(R.string.specific_engine_init_failed, "flEngine", flInitCode);
            Log.e("zxb", "initEngine: " + error);
            ZhumuToastUtil.showToast(error);
        }
    }

    @Override
    public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
        previewSize = camera.getParameters().getPreviewSize();
        drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation
                , cameraId, isMirror, false, false);
        Log.i("zxb", "onCameraOpened: " + drawHelper.toString());
        // 切换相机的时候可能会导致预览尺寸发生变化
        if (faceHelper == null) {
            Integer trackedFaceCount = null;
            // 记录切换时的人脸序号
            if (faceHelper != null) {
                trackedFaceCount = faceHelper.getTrackedFaceCount();
                faceHelper.release();
            }
            faceHelper = new FaceHelper.Builder()
                    .ftEngine(ftEngine)
                    .frEngine(frEngine)
                    .flEngine(flEngine)
                    .frQueueSize(Constants.MAX_DETECT_NUM)
                    .flQueueSize(Constants.MAX_DETECT_NUM)
                    .previewSize(previewSize)
                    .faceListener(arcFaceListener)
                    .trackedFaceCount(trackedFaceCount == null ? ConfigUtil.getTrackedFaceCount(mContext) : trackedFaceCount)
                    .build();
            arcFaceListener.setFaceHelper(faceHelper);
        }
    }

    @Override
    public void onPreview(byte[] nv21, Camera camera) {
        if (faceRectView != null) {
            faceRectView.clearFaceInfo();
        }
        List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21);
        if (facePreviewInfoList != null && faceRectView != null && drawHelper != null) {
            drawPreviewInfo(facePreviewInfoList, faceRectView, drawHelper, faceHelper);
        }
        clearLeftFace(facePreviewInfoList);

        if (facePreviewInfoList == null || facePreviewInfoList.size() < 1 || previewSize == null) {
            resultCallback.onFaceDismiss(0);
            return;
        }
        for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
            Integer status = arcFaceListener.getFeatureStatusMap().get(facePreviewInfo.getTrackId());
            /**
             * 在活体检测开启，在人脸识别状态不为成功或人脸活体状态不为处理中（ANALYZING）且不为处理完成（ALIVE、NOT_ALIVE）时重新进行活体检测
             */
            if (SettingUtils.getLivenewssDetect() && (status == null || status != RequestFeatureStatus.SUCCEED)) {
                Integer liveness = arcFaceListener.getLivnessStatusMap().get(facePreviewInfo.getTrackId());
                if (liveness == null || (liveness != LivenessInfo.ALIVE && liveness != LivenessInfo.NOT_ALIVE && liveness != RequestLivenessStatus.ANALYZING)) {
                    arcFaceListener.getLivnessStatusMap().put(facePreviewInfo.getTrackId(), RequestLivenessStatus.ANALYZING);
                    faceHelper.requestFaceLiveness(nv21, facePreviewInfo.getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfo.getTrackId(), LivenessType.RGB);
                }
            }
            /**
             * 对于每个人脸，若状态为空或者为失败，则请求特征提取（可根据需要添加其他判断以限制特征提取次数），
             * 特征提取回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer, Integer)}中回传
             */
            if (status == null || status == RequestFeatureStatus.TO_RETRY) {
                arcFaceListener.getFeatureStatusMap().put(facePreviewInfo.getTrackId(), RequestFeatureStatus.SEARCHING);
                faceHelper.requestFaceFeature(nv21, facePreviewInfo.getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfo.getTrackId());
//                            Log.i("zxb", "onPreview: fr start = " + System.currentTimeMillis() + " trackId = " + facePreviewInfoList.get(i).getTrackedFaceCount());
            }

        }
    }

    @Override
    public void onCameraClosed() {

    }

    @Override
    public void onCameraError(Exception e) {

    }

    @Override
    public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
        if (drawHelper != null) {
            drawHelper.setCameraDisplayOrientation(displayOrientation);
        }
        Log.i("zxb", "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
    }

    private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList, FaceRectView faceRectView, DrawHelper drawHelper, FaceHelper faceHelper) {
        List<DrawInfo> drawInfoList = new ArrayList<>();
        for (int i = 0; i < facePreviewInfoList.size(); i++) {
            String name = faceHelper.getName(facePreviewInfoList.get(i).getTrackId());
            Integer liveness = arcFaceListener.getLivnessStatusMap().get(facePreviewInfoList.get(i).getTrackId());
            Integer recognizeStatus = arcFaceListener.getFeatureStatusMap().get(facePreviewInfoList.get(i).getTrackId());

            // 根据识别结果和活体结果设置颜色
            int color = RecognizeColor.COLOR_UNKNOWN;
            if (recognizeStatus != null) {
                color = (recognizeStatus == RequestFeatureStatus.SUCCEED) ? RecognizeColor.COLOR_SUCCESS :
                        (recognizeStatus == RequestFeatureStatus.FAILED) ? RecognizeColor.COLOR_FAILED : color;
            }
            color = (liveness != null && liveness == LivenessInfo.NOT_ALIVE) ? RecognizeColor.COLOR_FAILED : color;

            drawInfoList.add(new DrawInfo(drawHelper.adjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect()),
                    GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE, liveness == null ? LivenessInfo.UNKNOWN : liveness, color,
                    name == null ? String.valueOf(facePreviewInfoList.get(i).getTrackId()) : name));
        }
        drawHelper.draw(faceRectView, drawInfoList);
    }

    /**
     * 删除已经离开的人脸
     *
     * @param facePreviewInfoList 人脸和trackId列表
     */
    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
            arcFaceListener.getFeatureStatusMap().clear();
            arcFaceListener.getLivnessStatusMap().clear();
            arcFaceListener.getErrorLivnessMap().clear();
            arcFaceListener.getErrorFeatureMap().clear();
            return;
        }
        Enumeration<Integer> keys = arcFaceListener.getFeatureStatusMap().keys();
        while (keys.hasMoreElements()) {
            int key = keys.nextElement();
            boolean contained = false;
            for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
                if (facePreviewInfo.getTrackId() == key) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                arcFaceListener.getFeatureStatusMap().remove(key);
                arcFaceListener.getLivnessStatusMap().remove(key);
                arcFaceListener.getErrorLivnessMap().remove(key);
                arcFaceListener.getErrorFeatureMap().remove(key);
            }
        }

    }

    private FaceRectView faceRectView;

    public ARCCameraListener setFaceRectView(FaceRectView faceRectView) {
        this.faceRectView = faceRectView;
        return this;
    }

    /**
     * 销毁引擎，faceHelper中可能会有特征提取耗时操作仍在执行，加锁防止crash
     */
    public void unInitEngine() {
        if (ftInitCode == ErrorInfo.MOK && ftEngine != null) {
            synchronized (ftEngine) {
                int ftUnInitCode = ftEngine.unInit();
                Log.e("zxb", "unInitEngine: " + ftUnInitCode);
            }
        }
        if (frInitCode == ErrorInfo.MOK && frEngine != null) {
            synchronized (frEngine) {
                int frUnInitCode = frEngine.unInit();
                Log.e("zxb", "unInitEngine: " + frUnInitCode);
            }
        }
        if (flInitCode == ErrorInfo.MOK && flEngine != null) {
            synchronized (flEngine) {
                int flUnInitCode = flEngine.unInit();
                Log.e("zxb", "unInitEngine: " + flUnInitCode);
            }
        }
    }

    public void ARCRelese(AppCompatActivity activity) {
        unInitEngine();
        if (faceHelper != null) {
            ConfigUtil.setTrackedFaceCount(activity, faceHelper.getTrackedFaceCount());
            faceHelper.release();
            faceHelper = null;
        }
        FaceServer.getInstance().unInit();
    }
    GetResultCallback resultCallback;
    public ARCCameraListener setRelultCallback(GetResultCallback relultCallback) {
        arcFaceListener.setResultListener(relultCallback);
        this.resultCallback = relultCallback;
        return this;
    }
}
