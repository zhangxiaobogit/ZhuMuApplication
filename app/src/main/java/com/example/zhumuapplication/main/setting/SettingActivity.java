package com.example.zhumuapplication.main.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zhumuapplication.R;
import com.example.zhumuapplication.util.SettingUtils;
import com.example.zhumuapplication.view.ChooseDetectDegreeDialog;

public class SettingActivity extends AppCompatActivity {
    private Switch sw_change_cameraid;
    private Switch sw_check_alive;

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
    }

    public void initView() {
        findViewById(R.id.tv_check_angle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDetectDegree();
            }
        });
        findViewById(R.id.tv_compare_score).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCompareSettingDialog();
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        sw_change_cameraid = findViewById(R.id.sw_change_cameraid);
        sw_change_cameraid.setChecked(SettingUtils.getCameraId() == 0 ? true : false);
        sw_change_cameraid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingUtils.setCameraId(isChecked ? 0 : 1);
            }
        });
        sw_check_alive = findViewById(R.id.sw_check_alive);
        sw_check_alive.setChecked(SettingUtils.getLivenewssDetect());
        sw_check_alive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
