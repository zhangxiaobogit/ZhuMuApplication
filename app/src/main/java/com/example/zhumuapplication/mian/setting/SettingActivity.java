package com.example.zhumuapplication.mian.setting;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.zhumuapplication.R;
import com.example.zhumuapplication.mian.BaseActivity;

public class SettingActivity extends BaseActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_setting);
    }

    @Override
    public void initView() {
        findViewById(R.id.tv_check_angle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

}
