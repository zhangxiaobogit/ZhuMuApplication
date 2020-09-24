package com.facecompare.zhumu.view;

import android.util.Log;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.LivenessInfo;
import com.facecompare.zhumu.common.Constants;
import com.facecompare.zhumu.common.dbentity.VisitorInfo;
import com.facecompare.zhumu.faceserver.FaceServer;
import com.facecompare.zhumu.main.compare.GetResultCallback;
import com.facecompare.zhumu.model.RequestFeatureStatus;
import com.facecompare.zhumu.util.SettingUtils;
import com.facecompare.zhumu.util.SoundUtils;
import com.facecompare.zhumu.util.face.FaceHelper;
import com.facecompare.zhumu.util.face.FaceListener;

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

/**
 * 描 述：
 * 作 者：zxb  2020-09-23 11:38
 * 修改描述： XXX
 * 修 改 人： XXX  2020-09-23 11:38
 * 修改版本： XXX
 */
public class ARCFaceListener implements FaceListener {
    /**
     * 用于存储活体值
     */
    private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();
    /**
     * 用于记录人脸特征提取出错重试次数
     */
    private ConcurrentHashMap<Integer, Integer> extractErrorRetryMap = new ConcurrentHashMap<>();
    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
    private ConcurrentHashMap<Integer, Integer> livenessErrorRetryMap = new ConcurrentHashMap<>();
    /**
     * 用于记录人脸识别相关状态
     */
    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();

    @Override
    public void onFail(Exception e) {

    }


    @Override
    public void onFaceFeatureInfoGet(FaceFeature faceFeature, Integer requestId, Integer errorCode) {
        //FR成功
        if (faceFeature != null) {
//                    Log.i("zxb", "onPreview: fr end = " + System.currentTimeMillis() + " trackId = " + requestId);
            Integer liveness = livenessMap.get(requestId);

            if (!SettingUtils.getLivenewssDetect()) {//不做活体检测的情况，直接搜索
                searchFace(faceFeature, requestId, resultCallback);
            } else if (liveness != null && liveness == LivenessInfo.ALIVE) {//活体检测通过，搜索特征
                searchFace(faceFeature, requestId, resultCallback);
            } else {//活体检测未出结果，或者非活体，延迟执行该函数
                if (requestFeatureStatusMap.containsKey(requestId)) {
                    Observable.timer(Constants.WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS)
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

        } else { //特征提取失败
            if (increaseAndGetValue(extractErrorRetryMap, requestId) > Constants.MAX_RETRY_TIME) {
                extractErrorRetryMap.put(requestId, 0);

                String msg;
                // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
                if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                    msg = "比对未通过";
                } else {
                    msg = "ExtractCode:" + errorCode;
                }
                faceHelper.setName(requestId, "未通过:" + msg);
                // 在尝试最大次数后，特征提取仍然失败，则认为识别未通过
                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                retryRecognizeDelayed(requestId);
            } else {
                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
            }
        }
    }

    @Override
    public void onFaceLivenessInfoGet(LivenessInfo livenessInfo, Integer requestId, Integer errorCode) {
        if (livenessInfo != null) {
            int liveness = livenessInfo.getLiveness();
            livenessMap.put(requestId, liveness);
            // 非活体，重试
            if (liveness == LivenessInfo.NOT_ALIVE) {
                faceHelper.setName(requestId, "未通过:NOT_ALIVE");
                // 延迟 FAIL_RETRY_INTERVAL 后，将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                retryLivenessDetectDelayed(requestId);
            }
        } else {
            if (increaseAndGetValue(livenessErrorRetryMap, requestId) > Constants.MAX_RETRY_TIME) {
                livenessErrorRetryMap.put(requestId, 0);
                String msg;
                // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
                if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                    msg = "人脸置信度低";
                } else {
                    msg = "ProcessCode:" + errorCode;
                }
                faceHelper.setName(requestId, "未通过:" + msg);
                retryLivenessDetectDelayed(requestId);
            } else {
                livenessMap.put(requestId, LivenessInfo.UNKNOWN);
            }
        }
    }

    private void searchFace(final FaceFeature frFace, final Integer requestId, final GetResultCallback resultCallback) {
        Observable
                .create((ObservableOnSubscribe<VisitorInfo>) emitter -> {
//                        Log.i("zxb", "subscribe: fr search start = " + System.currentTimeMillis() + " trackId = " + requestId);
                    VisitorInfo compareResult = FaceServer.getInstance().getTopOfFaceLib(frFace);
//                        Log.i("zxb", "subscribe: fr search end = " + System.currentTimeMillis() + " trackId = " + requestId);
                    emitter.onNext(compareResult);

                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(compareResult -> {
                    if (compareResult == null || compareResult.getVisitName() == null) {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                        faceHelper.setName(requestId, "VISITOR " + requestId);
                        return;
                    }

//                        Log.i("zxb", "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = " + requestId + "  similar = " + compareResult.getSimilar());
                    Log.e("zxb", " 比对分数  " + compareResult.getVisitCompareScore() + "  比对阈值 " + SettingUtils.getMoreCompareScore() + "  姓名  " + compareResult.getVisitName());
                    if (compareResult.getVisitCompareFlag().equals(Constants.COMPARE_SUCCESS)) {
                        SoundUtils.getInstance().playSelfCompareSound(true, compareResult.getVisitName());
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
                        faceHelper.setName(requestId, "通过" + compareResult.getVisitName());
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
                .create((ObservableOnSubscribe<VisitorInfo>) emitter -> {
//                        Log.i("zxb", "subscribe: fr search start = " + System.currentTimeMillis() + " trackId = " + requestId);
                    VisitorInfo compareResult = FaceServer.getInstance().getSingleCopmare(frFace, new FaceFeature());
//                        Log.i("zxb", "subscribe: fr search end = " + System.currentTimeMillis() + " trackId = " + requestId);
                    emitter.onNext(compareResult);

                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(compareResult -> {
                    if (compareResult == null || compareResult.getVisitName() == null) {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                        faceHelper.setName(requestId, "VISITOR " + requestId);
                        return;
                    }

//                        Log.i("zxb", "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = " + requestId + "  similar = " + compareResult.getSimilar());
                    Log.e("zxb", " 比对分数  " + compareResult.getVisitCompareScore() + "  比对阈值 " + SettingUtils.getMoreCompareScore() + "  姓名  " + compareResult.getVisitName());
                    if (compareResult.getVisitCompareFlag().equals(Constants.COMPARE_SUCCESS)) {
                        SoundUtils.getInstance().playSelfCompareSound(true, compareResult.getVisitName());
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
                        faceHelper.setName(requestId, "通过" + compareResult.getVisitName());
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
     * 延迟 FAIL_RETRY_INTERVAL 重新进行人脸识别
     * <p>
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

    private FaceHelper faceHelper;

    public ARCFaceListener setFaceHelper(FaceHelper faceHelper) {
        this.faceHelper = faceHelper;
        return this;
    }

    private GetResultCallback resultCallback;

    public ARCFaceListener setResultListener(GetResultCallback resultCallback) {
        this.resultCallback = resultCallback;
        return this;
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


    public ConcurrentHashMap<Integer, Integer> getFeatureStatusMap() {
        return requestFeatureStatusMap;
    }

    public ConcurrentHashMap<Integer, Integer> getLivnessStatusMap() {
        return livenessMap;
    }

    /**
     * 用于记录人脸特征提取出错重试次数
     */
    public ConcurrentHashMap<Integer, Integer> getErrorFeatureMap() {
        return extractErrorRetryMap;
    }

    /**
     * 用于记录活体人脸特征提取出错重试次数
     */
    public ConcurrentHashMap<Integer, Integer> getErrorLivnessMap() {
        return livenessErrorRetryMap;
    }

}
