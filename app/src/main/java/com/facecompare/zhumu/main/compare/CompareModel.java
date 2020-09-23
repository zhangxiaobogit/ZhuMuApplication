package com.facecompare.zhumu.main.compare;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.facecompare.zhumu.R;
import com.facecompare.zhumu.common.Constants;
import com.facecompare.zhumu.faceserver.CompareResult;
import com.facecompare.zhumu.faceserver.FaceServer;
import com.facecompare.zhumu.model.DrawInfo;
import com.facecompare.zhumu.model.FacePreviewInfo;
import com.facecompare.zhumu.model.RequestFeatureStatus;
import com.facecompare.zhumu.model.RequestLivenessStatus;
import com.facecompare.zhumu.util.ConfigUtil;
import com.facecompare.zhumu.util.DrawHelper;
import com.facecompare.zhumu.util.SettingUtils;
import com.facecompare.zhumu.util.SoundUtils;
import com.facecompare.zhumu.util.ZhumuToastUtil;
import com.facecompare.zhumu.util.camera.CameraHelper;
import com.facecompare.zhumu.util.camera.CameraListener;
import com.facecompare.zhumu.util.camera.RecognizeColor;
import com.facecompare.zhumu.util.face.FaceHelper;
import com.facecompare.zhumu.util.face.FaceListener;
import com.facecompare.zhumu.util.face.LivenessType;
import com.facecompare.zhumu.view.FaceRectView;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CompareModel {
    private static final String TAG = "CompareModel";


    /**
     * 用于存储活体值
     */
    private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();

    /**
     * 当FR成功，活体未成功时，FR等待活体的时间
     */
    private static final int WAIT_LIVENESS_INTERVAL = 100;
    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
    /**
     * 用于记录人脸特征提取出错重试次数
     */
    private ConcurrentHashMap<Integer, Integer> extractErrorRetryMap = new ConcurrentHashMap<>();

    private Camera.Size previewSize;
    private ConcurrentHashMap<Integer, Integer> livenessErrorRetryMap = new ConcurrentHashMap<>();

    private FaceHelper faceHelper;
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

        Log.i(TAG, "initEngine:  init: " + ftInitCode);

        if (ftInitCode != ErrorInfo.MOK) {
            String error = context.getString(R.string.specific_engine_init_failed, "ftEngine", ftInitCode);
            Log.i(TAG, "initEngine: " + error);
            ZhumuToastUtil.showToast(error);
        }
        if (frInitCode != ErrorInfo.MOK) {
            String error = context.getString(R.string.specific_engine_init_failed, "frEngine", frInitCode);
            Log.i(TAG, "initEngine: " + error);
            ZhumuToastUtil.showToast(error);
        }
        if (flInitCode != ErrorInfo.MOK) {
            String error = context.getString(R.string.specific_engine_init_failed, "flEngine", flInitCode);
            Log.i(TAG, "initEngine: " + error);
            ZhumuToastUtil.showToast(error);
        }
    }

    private DrawHelper drawHelper;

    public void initCamera(final AppCompatActivity activity, final FaceRectView faceRectView, final View previewView, final GetResultCallback getResultCallback) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final FaceListener faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e("zxb", "onFail: " + e.getMessage());
            }

            //请求FR的回调
            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId, final Integer errorCode) {
                //FR成功
                if (faceFeature != null) {
//                    Log.i("zxb", "onPreview: fr end = " + System.currentTimeMillis() + " trackId = " + requestId);
                    Integer liveness = livenessMap.get(requestId);
                    //不做活体检测的情况，直接搜索
                    if (!SettingUtils.getLivenewssDetect()) {
                        searchFace(faceFeature, requestId, getResultCallback);
                    }
                    //活体检测通过，搜索特征
                    else if (liveness != null && liveness == LivenessInfo.ALIVE) {
                        searchFace(faceFeature, requestId, getResultCallback);
                    }
                    //活体检测未出结果，或者非活体，延迟执行该函数
                    else {
                        if (requestFeatureStatusMap.containsKey(requestId)) {
                            Observable.timer(WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS)
                                    .subscribe(new io.reactivex.Observer<Long>() {
                                        Disposable disposable;

                                        @Override
                                        public void onSubscribe(Disposable d) {
                                            disposable = d;
                                            getFeatureDelayedDisposables.add(disposable);
                                        }

                                        @Override
                                        public void onNext(Long aLong) {
                                            onFaceFeatureInfoGet(faceFeature, requestId, errorCode);
                                        }

                                        @Override
                                        public void onError(Throwable e) {

                                        }

                                        @Override
                                        public void onComplete() {
                                            getFeatureDelayedDisposables.remove(disposable);
                                        }
                                    });

                        }
                    }

                }
                //特征提取失败
                else {
                    if (increaseAndGetValue(extractErrorRetryMap, requestId) > Constants.MAX_RETRY_TIME) {
                        extractErrorRetryMap.put(requestId, 0);

                        String msg;
                        // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
                        if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                            msg = activity.getString(R.string.low_confidence_level);
                        } else {
                            msg = "ExtractCode:" + errorCode;
                        }
                        faceHelper.setName(requestId, activity.getString(R.string.recognize_failed_notice, msg));
                        // 在尝试最大次数后，特征提取仍然失败，则认为识别未通过
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                        retryRecognizeDelayed(requestId);
                    } else {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                    }
                }
            }

            //活体检测结果回调
            @Override
            public void onFaceLivenessInfoGet(@Nullable LivenessInfo livenessInfo, final Integer requestId, Integer errorCode) {
                if (livenessInfo != null) {
                    int liveness = livenessInfo.getLiveness();
                    livenessMap.put(requestId, liveness);
                    // 非活体，重试
                    if (liveness == LivenessInfo.NOT_ALIVE) {
                        faceHelper.setName(requestId, activity.getString(R.string.recognize_failed_notice, "NOT_ALIVE"));
                        // 延迟 FAIL_RETRY_INTERVAL 后，将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                        retryLivenessDetectDelayed(requestId);
                    }
                } else {
                    if (increaseAndGetValue(livenessErrorRetryMap, requestId) > Constants.MAX_RETRY_TIME) {
                        livenessErrorRetryMap.put(requestId, 0);
                        String msg;
                        // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
                        if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                            msg = activity.getString(R.string.low_confidence_level);
                        } else {
                            msg = "ProcessCode:" + errorCode;
                        }
                        faceHelper.setName(requestId, activity.getString(R.string.recognize_failed_notice, msg));
                        retryLivenessDetectDelayed(requestId);
                    } else {
                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                    }
                }
            }


        };


        CameraListener cameraListener = new CameraListener() {
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
                            .faceListener(faceListener)
                            .trackedFaceCount(trackedFaceCount == null ? ConfigUtil.getTrackedFaceCount(activity.getApplicationContext()) : trackedFaceCount)
                            .build();
                }
            }


            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                if (faceRectView != null) {
                    faceRectView.clearFaceInfo();
                }
                List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21);
                if (facePreviewInfoList != null && faceRectView != null && drawHelper != null) {
                    drawPreviewInfo(facePreviewInfoList, faceRectView, drawHelper,faceHelper);
                }
                clearLeftFace(facePreviewInfoList);

                if (facePreviewInfoList == null || facePreviewInfoList.size() < 1 || previewSize == null)
                    return;
                for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
                    Integer status = requestFeatureStatusMap.get(facePreviewInfo.getTrackId());
                    /**
                     * 在活体检测开启，在人脸识别状态不为成功或人脸活体状态不为处理中（ANALYZING）且不为处理完成（ALIVE、NOT_ALIVE）时重新进行活体检测
                     */
                    if (SettingUtils.getLivenewssDetect() && (status == null || status != RequestFeatureStatus.SUCCEED)) {
                        Integer liveness = livenessMap.get(facePreviewInfo.getTrackId());
                        if (liveness == null || (liveness != LivenessInfo.ALIVE && liveness != LivenessInfo.NOT_ALIVE && liveness != RequestLivenessStatus.ANALYZING)) {
                            livenessMap.put(facePreviewInfo.getTrackId(), RequestLivenessStatus.ANALYZING);
                            faceHelper.requestFaceLiveness(nv21, facePreviewInfo.getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfo.getTrackId(), LivenessType.RGB);
                        }
                    }
                    /**
                     * 对于每个人脸，若状态为空或者为失败，则请求特征提取（可根据需要添加其他判断以限制特征提取次数），
                     * 特征提取回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer, Integer)}中回传
                     */
                    if (status == null || status == RequestFeatureStatus.TO_RETRY) {
                        requestFeatureStatusMap.put(facePreviewInfo.getTrackId(), RequestFeatureStatus.SEARCHING);
                        faceHelper.requestFaceFeature(nv21, facePreviewInfo.getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfo.getTrackId());
//                            Log.i("zxb", "onPreview: fr start = " + System.currentTimeMillis() + " trackId = " + facePreviewInfoList.get(i).getTrackedFaceCount());
                    }

                }
            }

            @Override
            public void onCameraClosed() {
                Log.i("zxb", "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i("zxb", "onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (drawHelper != null) {
                    drawHelper.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i("zxb", "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(previewView.getMeasuredWidth(), previewView.getMeasuredHeight()))
                .rotation(activity.getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(SettingUtils.getCameraId())
                .isMirror(SettingUtils.getCameraMirror())
                .previewOn(previewView)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
        cameraHelper.start();
    }

    private Bitmap idCardBitmap = null;

    public void setIdCardBitmap(Bitmap idPhoto) {
        idCardBitmap = idPhoto;
    }

    CameraHelper cameraHelper;


    private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList, FaceRectView faceRectView, DrawHelper drawHelper,FaceHelper faceHelper) {
        List<DrawInfo> drawInfoList = new ArrayList<>();
        for (int i = 0; i < facePreviewInfoList.size(); i++) {
            String name = faceHelper.getName(facePreviewInfoList.get(i).getTrackId());
            Integer liveness = livenessMap.get(facePreviewInfoList.get(i).getTrackId());
            Integer recognizeStatus = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());

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
     * 用于记录人脸识别相关状态
     */
    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();

    private void searchFace(final FaceFeature frFace, final Integer requestId, final GetResultCallback resultCallback) {
        Observable
                .create((ObservableOnSubscribe<CompareResult>) emitter -> {
//                        Log.i("zxb", "subscribe: fr search start = " + System.currentTimeMillis() + " trackId = " + requestId);
                    CompareResult compareResult = FaceServer.getInstance().getTopOfFaceLib(frFace);
//                        Log.i("zxb", "subscribe: fr search end = " + System.currentTimeMillis() + " trackId = " + requestId);
                    emitter.onNext(compareResult);

                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(compareResult -> {
                    if (compareResult == null || compareResult.getUserName() == null) {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                        faceHelper.setName(requestId, "VISITOR " + requestId);
                        return;
                    }

//                        Log.i("zxb", "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = " + requestId + "  similar = " + compareResult.getSimilar());
                    Log.e("zxb", " 比对分数  " + ((int) (compareResult.getSimilar() * 100)) + "  比对阈值 " + SettingUtils.getMoreCompareScore() + "  姓名  " + compareResult.getUserName());
                    if (((int) (compareResult.getSimilar() * 100)) > SettingUtils.getMoreCompareScore()) {
                        SoundUtils.getInstance().playSelfCompareSound(true, compareResult.getUserName());
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
                        faceHelper.setName(requestId, "通过" + compareResult.getUserName());
                        resultCallback.getCompareResultCall(compareResult);
                    } else {
                        faceHelper.setName(requestId, "未通过");
                        retryRecognizeDelayed(requestId);
                    }

                }, throwable -> {
                    faceHelper.setName(requestId, "未通过");
                    retryRecognizeDelayed(requestId);
                });

    }


    private void singleCompare(final FaceFeature frFace, final Integer requestId, final GetResultCallback resultCallback) {
        Observable
                .create((ObservableOnSubscribe<CompareResult>) emitter -> {
//                        Log.i("zxb", "subscribe: fr search start = " + System.currentTimeMillis() + " trackId = " + requestId);
                    CompareResult compareResult = FaceServer.getInstance().getSingleCopmare(frFace, new FaceFeature());
//                        Log.i("zxb", "subscribe: fr search end = " + System.currentTimeMillis() + " trackId = " + requestId);
                    emitter.onNext(compareResult);

                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(compareResult -> {
                    if (compareResult == null || compareResult.getUserName() == null) {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                        faceHelper.setName(requestId, "VISITOR " + requestId);
                        return;
                    }

//                        Log.i("zxb", "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = " + requestId + "  similar = " + compareResult.getSimilar());
                    Log.e("zxb", " 比对分数  " + ((int) (compareResult.getSimilar() * 100)) + "  比对阈值 " + SettingUtils.getMoreCompareScore() + "  姓名  " + compareResult.getUserName());
                    if (((int) (compareResult.getSimilar() * 100)) > SettingUtils.getMoreCompareScore()) {
                        SoundUtils.getInstance().playSelfCompareSound(true, compareResult.getUserName());
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
                        faceHelper.setName(requestId, "通过" + compareResult.getUserName());
                        resultCallback.getCompareResultCall(compareResult);
                    } else {
                        faceHelper.setName(requestId, "未通过");
                        retryRecognizeDelayed(requestId);
                    }

                }, throwable -> {
                    faceHelper.setName(requestId, "未通过");
                    retryRecognizeDelayed(requestId);
                });

    }

    /**
     * 注册人脸状态码，注册结束（无论成功失败）
     */
    private static final int REGISTER_STATUS_DONE = 2;
    private int registerStatus = REGISTER_STATUS_DONE;
    /**
     * 注册人脸状态码，准备注册
     */
    private static final int REGISTER_STATUS_READY = 0;
    /**
     * 注册人脸状态码，注册中
     */
    private static final int REGISTER_STATUS_PROCESSING = 1;


    /**
     * 延迟 FAIL_RETRY_INTERVAL 重新进行活体检测
     *
     * @param requestId 人脸ID
     */
    private void retryLivenessDetectDelayed(final Integer requestId) {
        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                        if (SettingUtils.getLivenewssDetect()) {
                            faceHelper.setName(requestId, Integer.toString(requestId));
                        }
                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }


    /**
     * 延迟 FAIL_RETRY_INTERVAL 重新进行人脸识别
     *
     * 失败重试间隔时间（ms）
     */
    private static final long FAIL_RETRY_INTERVAL = 1000;
    private CompositeDisposable delayFaceTaskCompositeDisposable = new CompositeDisposable();

    private void retryRecognizeDelayed(final Integer requestId) {
        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(new io.reactivex.Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸特征提取状态置为FAILED，帧回调处理时会重新进行活体检测
                        faceHelper.setName(requestId, Integer.toString(requestId));
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    /**
     * 将map中key对应的value增1回传
     *
     * @param countMap map
     * @param key      key
     * @return 增1后的value
     */
    public int increaseAndGetValue(Map<Integer, Integer> countMap, int key) {
        if (countMap == null) {
            return 0;
        }
        Integer value = countMap.get(key);
        if (value == null) {
            value = 0;
        }
        countMap.put(key, ++value);
        return value;
    }

    /**
     * 删除已经离开的人脸
     *
     * @param facePreviewInfoList 人脸和trackId列表
     */
    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
            requestFeatureStatusMap.clear();
            livenessMap.clear();
            livenessErrorRetryMap.clear();
            extractErrorRetryMap.clear();
            if (getFeatureDelayedDisposables != null) {
                getFeatureDelayedDisposables.clear();
            }
            return;
        }
        Enumeration<Integer> keys = requestFeatureStatusMap.keys();
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
                requestFeatureStatusMap.remove(key);
                livenessMap.remove(key);
                livenessErrorRetryMap.remove(key);
                extractErrorRetryMap.remove(key);
            }
        }

    }

    public void ARCRelese(AppCompatActivity activity) {
        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }

        unInitEngine();
        if (getFeatureDelayedDisposables != null) {
            getFeatureDelayedDisposables.clear();
        }
        if (delayFaceTaskCompositeDisposable != null) {
            delayFaceTaskCompositeDisposable.clear();
        }
        if (faceHelper != null) {
            ConfigUtil.setTrackedFaceCount(activity, faceHelper.getTrackedFaceCount());
            faceHelper.release();
            faceHelper = null;
        }

        FaceServer.getInstance().unInit();
    }

    /**
     * 销毁引擎，faceHelper中可能会有特征提取耗时操作仍在执行，加锁防止crash
     */
    private void unInitEngine() {
        if (ftInitCode == ErrorInfo.MOK && ftEngine != null) {
            synchronized (ftEngine) {
                int ftUnInitCode = ftEngine.unInit();
                Log.i(TAG, "unInitEngine: " + ftUnInitCode);
            }
        }
        if (frInitCode == ErrorInfo.MOK && frEngine != null) {
            synchronized (frEngine) {
                int frUnInitCode = frEngine.unInit();
                Log.i(TAG, "unInitEngine: " + frUnInitCode);
            }
        }
        if (flInitCode == ErrorInfo.MOK && flEngine != null) {
            synchronized (flEngine) {
                int flUnInitCode = flEngine.unInit();
                Log.i(TAG, "unInitEngine: " + flUnInitCode);
            }
        }
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
