package com.facecompare.zhumu.main.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.facecompare.zhumu.R;
import com.facecompare.zhumu.main.BaseActivity;
import com.facecompare.zhumu.util.SettingUtils;
import com.facecompare.zhumu.util.SwitchUtil;
import com.facecompare.zhumu.util.ZhumuToastUtil;
import com.facecompare.zhumu.view.ChooseDetectDegreeDialog;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SettingActivity extends BaseActivity {
    @BindView(R.id.sw_change_cameraid)
    Switch sw_change_cameraid;
    @BindView(R.id.sw_check_alive)
    Switch sw_check_alive;
    @BindView(R.id.sw_set_mirror)
    Switch sw_set_mirror;


    @BindView(R.id.tv_check_angle)
    TextView tv_check_angle;
    @BindView(R.id.tv_compare_score)
    TextView tv_compare_score;
    @BindView(R.id.iv_back)
    ImageView iv_back;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        initView();
    }

    public void initView() {
        tv_check_angle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDetectDegree();
            }
        });
        tv_compare_score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCompareSettingDialog();
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        SwitchUtil.switchOff(sw_change_cameraid, SettingUtils.getCameraId() == 0 ? true : false, new SwitchUtil.SetSwitchCall() {
            @Override
            public void setSwitchListener(boolean isChecked) {
                if (Camera.getNumberOfCameras() > 1) {
                    SettingUtils.setCameraId(isChecked ? 0 : 1);
                } else {
                    ZhumuToastUtil.showToast("当前仅有一个摄像头，切换无效");
                }
            }
        });
        SwitchUtil.switchOff(sw_set_mirror, SettingUtils.getCameraMirror(), new SwitchUtil.SetSwitchCall() {
            @Override
            public void setSwitchListener(boolean isChecked) {
                SettingUtils.setCameraMirror(isChecked);
            }
        });
        SwitchUtil.switchOff(sw_check_alive, SettingUtils.getLivenewssDetect(), new SwitchUtil.SetSwitchCall() {
            @Override
            public void setSwitchListener(boolean isChecked) {
                SettingUtils.setLivenewssDetect(isChecked);
            }
        });
        findViewById(R.id.tv_face_manger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, FaceManageActivity.class));
            }
        });

    }

    // 修改配置项的对话框
    ChooseDetectDegreeDialog chooseDetectDegreeDialog;

    public void chooseDetectDegree() {
        if (chooseDetectDegreeDialog == null) {
            chooseDetectDegreeDialog = new ChooseDetectDegreeDialog();
        }
        if (chooseDetectDegreeDialog.isAdded()) {
            chooseDetectDegreeDialog.dismiss();
        }
        chooseDetectDegreeDialog.show(getSupportFragmentManager(), ChooseDetectDegreeDialog.class.getSimpleName());
    }

    private void showCompareSettingDialog() {
        LayoutInflater factory = LayoutInflater.from(getApplicationContext());
        final View view = factory.inflate(R.layout.dialog_input_setting, null);
        TextView singleTv = view.findViewById(R.id.dialog_server_ip_tv);
        singleTv.setText("单人比对");
        TextView groupTv = view.findViewById(R.id.dialog_server_port_tv);
        groupTv.setText("多人比对");
        final EditText singleEditText = view.findViewById(R.id.dialog_server_ip);
        singleEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        final EditText groupEditText = view.findViewById(R.id.dialog_server_port);
        groupEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        singleEditText.setText(SettingUtils.getSingleCompareScore() + "");
        groupEditText.setText(SettingUtils.getMoreCompareScore() + "");
        new AlertDialog.Builder(SettingActivity.this)
                .setTitle("比对阈值设置")
                .setView(view)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SettingUtils.setSingleCompareScore(Integer.parseInt(singleEditText.getText().toString()));
                                SettingUtils.setMoreCompareScore(Integer.parseInt(groupEditText.getText().toString()));
                            }
                        }).setNegativeButton("取消", null).create().show();

    }

}
