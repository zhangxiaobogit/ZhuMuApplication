package com.example.zhumuapplication.main.compare;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.csht.ReadCardInit;
import com.csht.common.listener.ReadCardListener;
import com.csht.netty.entry.IdCard;
import com.csht.netty.entry.Info;
import com.cshtface.sdk.bean.msg;
import com.example.zhumuapplication.R;
import com.example.zhumuapplication.common.Constants;
import com.example.zhumuapplication.faceserver.FaceServer;
import com.example.zhumuapplication.main.BaseActivity;
import com.example.zhumuapplication.main.setting.SettingActivity;
import com.example.zhumuapplication.util.AdBannerShow;
import com.example.zhumuapplication.util.BaseCompareUtil;
import com.example.zhumuapplication.util.ImageUtil;
import com.example.zhumuapplication.util.TextToSpeechUtils;
import com.example.zhumuapplication.util.ZhumuToastUtil;
import com.example.zhumuapplication.view.CameraView;
import com.example.zhumuapplication.view.FaceOverlayViews;
import com.example.zhumuapplication.view.FaceRectView;
import com.example.zhumuapplication.view.ImageViewRoundOval;
import com.youth.banner.Banner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends BaseActivity implements CameraView.OnCameraStateChangedListener, ViewTreeObserver.OnGlobalLayoutListener {
    private boolean isPushCard = false;
    private String idNum;
    private byte[] idHeadByte;
    private byte[] idCardFaces;
    public static ExecutorService mainExecutors;
    private View previewView;
    /**
     * 绘制人脸框的控件
     */
    private FaceRectView faceRectView;
    private CompareModel compareModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        AdBannerShow.getInstance().initAdBanner((Banner) findViewById(R.id.banner_adv));
        compareModel = new CompareModel();
        initNfcReader();
        initView();
        initReadCard();
        if (mainExecutors == null) {
            mainExecutors = Executors.newCachedThreadPool();
        }
    }

    public void initView() {
//        initCameraView((CameraView) findViewById(R.id.cameraview), (FaceOverlayViews) findViewById(R.id.face_overlay_view), this);
        previewView = findViewById(R.id.single_camera_texture_preview);
        //在布局结束后才做初始化操作
        previewView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        faceRectView = findViewById(R.id.single_camera_face_rect_view);
        ((ImageViewRoundOval) findViewById(R.id.iv_head)).setType(ImageViewRoundOval.TYPE_CIRCLE);
        ((TextClock) findViewById(R.id.tc_clock)).setFormat24Hour("yyyy年 MM月 dd日");
        findViewById(R.id.tv_title).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                return false;
            }
        });
        //本地人脸库初始化
        FaceServer.getInstance().init(this);
    }


    public void initCameraView(CameraView cameraView, FaceOverlayViews faceOverlayView, AppCompatActivity activity) {
        if (faceOverlayView != null) {
            cameraView.setFaceView(faceOverlayView);
        }
        cameraView.initCamera(activity);
        cameraView.setOnCameraStateChangedListener(this);
    }

    private ReadCardInit readCardInit;

    private void initReadCard() {
        readCardInit = new ReadCardInit(this, "yjm.zrgk.com.cn",
                8222, "ttyS3", "人证核验", new ReadCardListener() {
            @Override
            public void onReadCardStateChanged(int i, int i1, String s) {

            }

            @Override
            public void onReadCardSuccess(int i, final IdCard idCard, Info info) {
                isPushCard = true;
                idNum = idCard.getId();
                idHeadByte = ImageUtil.bitmap2Bytes(idCard.getPhoto());
                ((ImageView) findViewById(R.id.iv_head)).setImageBitmap(idCard.getPhoto());
                ((TextView) findViewById(R.id.tv_name)).setText(idCard.getName());
                mainExecutors.execute(new Runnable() {
                    @Override
                    public void run() {
                        ImageUtil.saveIdCardImage(Constants.ZHUMU_IDCARD_PHOTO_DIRECTORY + idNum + ".jpg", idCard.getPhoto());//保存图片
                    }
                });
                Toast.makeText(MainActivity.this, idCard.getName(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onReadCardFail(int i, int i1, String s) {

            }

            @Override
            public void onReadIcCardSuccess(int i, String s) {
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();

            }

            @Override
            public void onReadIcCardFail(int i, int i1, String s) {

            }
        });
        readCardInit.setIsIcCard(true);
    }

    @Override
    public void onCameraError(int code) {

    }

    public PendingIntent mPendingIntent;
    public NfcAdapter nfcAdapter;
    public boolean isAndroidNFC = false;

    public void initNfcReader() {
        try {
            NfcManager manager = (NfcManager) getApplicationContext().getSystemService(Context.NFC_SERVICE);
            if (manager != null) {
                nfcAdapter = manager.getDefaultAdapter();
            } else {
                isAndroidNFC = false;
            }
//        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
            Log.e("zxb", " startUp  开始检测系统NFC模块");
            if (nfcAdapter != null) {
                Log.e("zxb", " startUp  检测到系统NFC_Adapter模块，开始加载");
                mPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this,
                        getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                isAndroidNFC = true;
            } else {
                isAndroidNFC = false;
                Log.e("zxb", " startUp   未检测到系统NFC模块");
            }
        } catch (Exception e) {
            isAndroidNFC = false;
            nfcAdapter = null;
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && readCardInit != null) {
            readCardInit.readCard(intent);
        }
    }

    @Override
    public void onCurrentPreviewFrame(final byte[] frame, final int width, final int height, int angle, Rect rect) {
        if (isPushCard) {
            msg[] nowFaces = BaseCompareUtil.getInstance().getCheckFaceMSG(frame, rect, angle);
            if (!((-40.0f < nowFaces[0].roll && nowFaces[0].roll < 40.0f)
                    && (50.0f > nowFaces[0].pitch && nowFaces[0].pitch > -50.0f)
                    && (-45.0f < nowFaces[0].yaw && nowFaces[0].yaw < 45.0f))) {
                Toast.makeText(MainActivity.this, "请对准摄像头", Toast.LENGTH_LONG).show();
                return;
            }

            final byte[] feature = BaseCompareUtil.getInstance().getFaceDataFeature(nowFaces, true);
            if (feature != null) {
                if (idCardFaces == null) {
                    idCardFaces = BaseCompareUtil.getInstance().getFeatureFromData(idHeadByte, false);
                }
                final float[] scores = BaseCompareUtil.getInstance().getNNCompareScore(feature, idCardFaces);//进行单人比对
                final int getScore = ((int) (scores[0] * 100));
                if (getScore > 60) {
                    TextToSpeechUtils.getInstance().speak("验证成功");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                } else {
                    TextToSpeechUtils.getInstance().speak("验证失败");
                }
            }
        }
    }

    @Override
    public void onFaceDismiss() {
    }

    @Override
    public void onCheckLiveness(boolean flag) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        compareModel.cameraStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        compareModel.cameraStop();
    }

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;

    @Override
    public void onGlobalLayout() {
        previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            compareModel.initEngine(this);
            compareModel.initCamera(this, faceRectView, previewView);
        }
    }


    @Override
    public void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                compareModel.initEngine(this);
                compareModel.initCamera(this, faceRectView, previewView);
            } else {
                ZhumuToastUtil.showToast(getString(R.string.permission_denied));
            }
        }
    }

    @Override
    protected void onDestroy() {
        compareModel.ARCRelese(this);
        super.onDestroy();
    }
}