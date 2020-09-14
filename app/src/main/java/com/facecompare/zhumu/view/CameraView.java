package com.facecompare.zhumu.view;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.csht.cshtfacetrack.Constants;
import com.csht.cshtfacetrack.CshtFaceTrackApi;
import com.csht.cshtfacetrack.CshtFaceTrackResult;
import com.csht.cshthardwarecontrol.api.LightApi;
import com.facecompare.zhumu.util.CameraUtil;
import com.facecompare.zhumu.util.NdkLoader;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by Administrator on 2017/3/29.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback, Camera.ErrorCallback {
    ExecutorService arcThreadPool = Executors.newSingleThreadExecutor();
    ExecutorService checkBrightnessThread = Executors.newSingleThreadExecutor();
    public OnCameraStateChangedListener onCameraStateChangedListener;
    SurfaceHolder surfaceHolder;
    Camera camera;
    Context context;
    public int screenWidth, screenHeight, previewWidth, previewHeight;
    public int cameraPosition = 0;
    private int mDisplayOrientation;
    private int mDisplayRotation;
    FaceOverlayViews mFaceView;
    int angle = 0;
    private boolean findFaceFlag = true;
    boolean isCatchFace = false;
    int faceDismissCount = 0;
    CshtFaceTrackApi cshtFaceTrackApi;
    int fps = 15;
    int calFrame = 0;
    long startTime = 0;
    boolean isFpsGot = false;
    boolean checkLivenessOpen = true;
    boolean isMixComparing = false;
    long lastCheckBrightnessTime = 0;
    long frameCount = 0;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {

        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initFaceTrackSdk();
        initScreen(context);
        initHolder();
    }

    private void initFaceTrackSdk() {
        cshtFaceTrackApi = new CshtFaceTrackApi(context, AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, 6, 1, false);
    }

    public void initCamera(AppCompatActivity activity) {
        if (camera == null) {
            try {
                faceDismissCount = 0;
                isCatchFace = false;
                int numberOfCameras = Camera.getNumberOfCameras();
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < numberOfCameras; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {

                    } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        // 永惠炮筒打开，其他注释
//                        if (mFaceView != null)
//                            mFaceView.setFront(true);//如果是前置摄像头设置，追踪框
                    }
                }
                if (camera == null) {
                    if (numberOfCameras > 1) {
                        cameraPosition = 0;
                    } else {
                        cameraPosition = 0;
                    }
                    camera = Camera.open(cameraPosition);
                    setDisplayOrientation(activity);
                }
                if (camera != null) {
                    Camera.Parameters parameters = camera.getParameters();
//                    if (parameters.isZoomSupported()){
//                        parameters.setZoom(parameters.getMaxZoom()/2);
//                    }

//                    Camera.Size size = CameraUtil.findBestPreviewResolution(parameters, 640, 480);

//                    Camera.Size size = CameraUtil.findMax43Resolution(parameters);
//                    List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
//                    Log.e("zxb","  相机  "+sizes.toString());
//                    for(Camera.Size size : sizes){
//                        Log.e("zxb","  相机 大小  "+size.height + "   " + size.width);
//                    }
                    Camera.Size size = null;
                    if (Build.DEVICE.equals("CS-RK3288ZYA")) {//众云主板
                        size = CameraUtil.findNearest43Resolution(parameters, 1024, 768);
                    } else {

                        //永惠炮筒打开
//                        size = CameraUtil.findNearest43Resolution(parameters, 640, 480);
                        //其他
                        size = CameraUtil.findNearest43Resolution(parameters, 800, 600);
                    }
                    parameters.setPreviewSize(size.width, size.height);
                    mFaceView.setPreviewWidth(size.width);
                    mFaceView.setPreviewHeight(size.height);
                    Log.i("zxb", "===size:" + size.width + " " + size.height);

//                    parameters.setPreviewSize(640, 480);
//                    mFaceView.setPreviewWidth(640);
//                    mFaceView.setPreviewHeight(480);

                    setAutoFocus(parameters);
                    previewWidth = parameters.getPreviewSize().width;
                    previewHeight = parameters.getPreviewSize().height;
                    parameters.setPictureFormat(PixelFormat.JPEG);
                    parameters.setJpegQuality(100);
                    camera.setErrorCallback(this);
                    camera.setPreviewCallbackWithBuffer(this);
                    camera.addCallbackBuffer(new byte[((parameters.getPreviewSize().width * parameters.getPreviewSize().height) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8]);

                    camera.setParameters(parameters);
                    try {
                        camera.setPreviewDisplay(surfaceHolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    camera.cancelAutoFocus();
                    camera.startPreview();
                    startTime = System.currentTimeMillis();
                }
            } catch (Exception e) {
                if (onCameraStateChangedListener != null) {
                    onCameraStateChangedListener.onCameraError(1);
                }
                Log.e("zxb", "run->  摄像头异常 ： " + e.toString());
                e.printStackTrace();
                reboot();
            }
        }
    }


    private void setAutoFocus(Camera.Parameters cameraParameters) {
        List<String> focusModes = cameraParameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    private void initHolder() {
        surfaceHolder = getHolder();
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.setFixedSize(screenWidth, screenHeight);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);
    }

    private void initScreen(Context context) {
        WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        WM.getDefaultDisplay().getMetrics(outMetrics);
        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;
    }


    private void setDisplayOrientation(AppCompatActivity activity) {
        mDisplayRotation = CameraUtil.getDisplayRotation(activity);
        mDisplayOrientation = CameraUtil.getDisplayOrientation(mDisplayRotation, cameraPosition);
        Log.i("zxb", "===Orientation " + mDisplayOrientation);
//        camera.setDisplayOrientation(mDisplayOrientation);
//        mDisplayOrientation = 0;


        if (mDisplayOrientation == 90) {
            //tps
            camera.setDisplayOrientation(90);
        }
        if (mDisplayOrientation == 270) {
            //炮筒
            camera.setDisplayOrientation(270);
        }

        if (mFaceView != null) {
            mFaceView.setDisplayOrientation(mDisplayOrientation);
        }


    }


    public void releaseCamera() {
        isCatchFace = false;
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (camera != null)
                camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }


    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        camera.addCallbackBuffer(data);

//        File file2 = new File("sdcard/aa.jpg");
//        if (!file2.exists()) {
//            ImageUtil.saveBitmapFile(ImageUtil.yuv2Rgb(data, previewWidth, previewHeight), file2);
//        }

        //每3帧检测一次
        frameCount++;
        if (frameCount % 3 != 0) {
            if (frameCount > 100000) {
                frameCount = 0;
            }
            return;
        }


        if (findFaceFlag) {
            arcThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    if (cshtFaceTrackApi != null) {
                        List<Rect> result;

//                        if (mDisplayOrientation == 270) {//永惠炮筒打开
                        if ((mDisplayOrientation == 90 && mDisplayOrientation == 270)) {//其他
                            //此条件暂时作废，无法进入
                            if (isMixComparing) {
                                return;
                            }
                            byte[] rotateData = null;

                            if (mDisplayOrientation == 90) {
                                rotateData = NdkLoader.rotateYUVDegree270(data, previewWidth, previewHeight);
                            }
                            if (mDisplayOrientation == 270) {
                                rotateData = NdkLoader.rotateYUVDegree90(data, previewWidth, previewHeight);
//                                File file3 = new File("sdcard/bb.jpg");
//                                if (!file3.exists()) {
//                                    ImageUtil.saveBitmapFile(ImageUtil.yuv2Rgb(rotateData, previewHeight, previewWidth), file3);
//                                }
//                                File file4 = new File("sdcard/cc.jpg");
//                                if (!file4.exists()) {
//                                    ImageUtil.saveBitmapFile(ImageUtil.yuv2Rgb(rotateData, previewWidth, previewHeight), file4);
//                                }
                            }
                            CshtFaceTrackResult cshtFaceTrackResult = cshtFaceTrackApi.detectYuvFrame(rotateData, previewHeight, previewWidth, 2050, checkLivenessOpen);//其他
                            result = cshtFaceTrackResult.getList();

                            int liveness = cshtFaceTrackResult.getLiveness();
                            if (mFaceView != null) {
                                mFaceView.setFaces(result);
                                if (result.size() > 0) {
                                    isCatchFace = true;
                                    faceDismissCount = 0;
                                    if (liveness == Constants.LIVENESS_LIVE || liveness == Constants.LIVENESS_NOT_ACTIVE) {

                                        if (onCameraStateChangedListener != null) {
                                            onCameraStateChangedListener.onCheckLiveness(true);
                                            onCameraStateChangedListener.onCurrentPreviewFrame(rotateData, previewHeight, previewWidth, angle, result.get(0));

                                        }
                                    } else if (liveness == Constants.LIVENESS_NOT_LIVE) {
                                        if (onCameraStateChangedListener != null) {
                                            onCameraStateChangedListener.onCheckLiveness(false);
                                        }
                                    } else {
                                        if (onCameraStateChangedListener != null) {
                                            onCameraStateChangedListener.onCheckLiveness(false);
                                        }
                                    }
                                } else {
                                    if (isCatchFace) {
                                        faceDismissCount++;
                                        if (faceDismissCount > fps / 2) {
                                            isCatchFace = false;
                                            faceDismissCount = 0;
                                            onCameraStateChangedListener.onFaceDismiss();
                                        }
                                    }
                                }
                            }
                        } else {

                            CshtFaceTrackResult cshtFaceTrackResult = cshtFaceTrackApi.detectYuvFrame(data, previewWidth, previewHeight, 2050, checkLivenessOpen);
                            result = cshtFaceTrackResult.getList();

                            if (mDisplayOrientation == 270) {
                                rotate90(result);
                            }

                            int liveness = cshtFaceTrackResult.getLiveness();
                            if (mFaceView != null) {
                                mFaceView.setFaces(result);
                                if (result.size() > 0) {
                                    isCatchFace = true;
                                    faceDismissCount = 0;
                                    if (liveness == Constants.LIVENESS_LIVE || liveness == Constants.LIVENESS_NOT_ACTIVE) {
                                        if (onCameraStateChangedListener != null) {
                                            onCameraStateChangedListener.onCheckLiveness(true);

//                                            onCameraStateChangedListener.onCurrentPreviewFrame(data, previewWidth, previewHeight, angle, result.get(0));


                                            if (mDisplayOrientation == 270) {
                                                byte[] rotateData = NdkLoader.rotateYUVDegree90(data, previewWidth, previewHeight);
                                                onCameraStateChangedListener.onCurrentPreviewFrame(rotateData, previewHeight, previewWidth, angle, result.get(0));
                                            } else {
                                                onCameraStateChangedListener.onCurrentPreviewFrame(data, previewWidth, previewHeight, angle, result.get(0));
                                            }


                                        }
                                    } else if (liveness == Constants.LIVENESS_NOT_LIVE) {
                                        if (onCameraStateChangedListener != null) {
                                            onCameraStateChangedListener.onCheckLiveness(false);
                                        }
                                    } else {
                                        if (onCameraStateChangedListener != null) {
                                            onCameraStateChangedListener.onCheckLiveness(false);
                                        }
                                    }
                                } else {
                                    if (isCatchFace) {
                                        faceDismissCount++;
                                        if (faceDismissCount > fps / 2) {
                                            isCatchFace = false;
                                            faceDismissCount = 0;
                                            onCameraStateChangedListener.onFaceDismiss();
                                        }
                                    }
                                }
                            }

                        }


                        getFps();
//                        getBrightness(data);

                    }

                }
            });
        }


    }

    private void getBrightness(final byte[] data) {
        checkBrightnessThread.execute(new Runnable() {
            @Override
            public void run() {
                long nowTime = System.currentTimeMillis();
                if (nowTime - lastCheckBrightnessTime > 60 * 60 * 1000) {
                    int b1 = NdkLoader.calYuvBrightness(data, previewWidth, previewHeight);
//                    EventBus.getDefault().post(new TestDataEvent(b1 + ""));
                    if (b1 < 120) {
                        lastCheckBrightnessTime = nowTime;
                        LightApi.getInstance().turnOn();
                    } else {
                        LightApi.getInstance().turnOff();
                        SystemClock.sleep(2000);
                        lastCheckBrightnessTime = nowTime;
                        int b2 = NdkLoader.calYuvBrightness(data, previewWidth, previewHeight);
                        if (b2 < 120) {
                            LightApi.getInstance().turnOn();
                        } else {
                            LightApi.getInstance().turnOff();
                        }
                    }
                }
            }
        });

    }

    private void getFps() {
        if (!isFpsGot) {
            if ((System.currentTimeMillis() - startTime) < 5000) {
                calFrame++;
            } else {
                fps = calFrame / 5 + 5;
                isFpsGot = true;
                Log.i("zxb", "===fps:" + fps);
            }
        }
    }


    public void setOnCameraStateChangedListener(OnCameraStateChangedListener onCameraStateChangedListener) {
        this.onCameraStateChangedListener = onCameraStateChangedListener;
    }

    @Override
    public void onError(int error, Camera camera) {
        if (onCameraStateChangedListener != null) {
            onCameraStateChangedListener.onCameraError(error);
        }
        reboot();
    }

    private void reboot() {
        Toast.makeText(context, "相机连接失败,即将重启设备", Toast.LENGTH_SHORT).show();
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("zxb", "close ->相机异常   硬件重启 --hardwareReboot()");
//                SystemUtil.hardwareReboot();
            }
        }, 2000);
    }

    public interface OnCameraStateChangedListener {
        void onCameraError(int code);

        void onCurrentPreviewFrame(byte[] frame, int w, int h, int angle, Rect rect);

        void onFaceDismiss();

        void onCheckLiveness(boolean flag);
    }

    public void setFaceView(FaceOverlayViews mFaceView) {
        this.mFaceView = mFaceView;
    }

    public void setFindFaceFlag(boolean findFaceFlag) {
        this.findFaceFlag = findFaceFlag;
    }

    public void setCheckLivenessOpen(boolean checkLivenessOpen) {
        this.checkLivenessOpen = checkLivenessOpen;
    }

    public void setMixComparing(boolean mixComparing) {
        isMixComparing = mixComparing;
    }

    void rotate90(List<Rect> result) {
        for (Rect rect : result) {
            int p = previewHeight;
            int x1 = rect.left;
            int y1 = rect.top;
            int newX1 = p - y1 - rect.width();
            int newY1 = x1;
            int x2 = rect.right;
            int y2 = rect.bottom;
            int newX2 = p - y2 + rect.width();
            int newY2 = x2;

            rect.left = newX1;
            rect.top = newY1;
            rect.right = newX2;
            rect.bottom = newY2;
        }
    }
}
