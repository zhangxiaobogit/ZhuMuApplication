package com.facecompare.zhumu.util;

import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * 描 述：
 * 作 者：zxb  2020-09-14 11:30
 * 修改描述： XXX
 * 修 改 人： XXX  2020-09-14 11:30
 * 修改版本： XXX
 */
public class SwitchUtil {
    public static void switchOff(Switch swView, boolean defaultValue, final SetSwitchCall switchCall) {
        swView.setChecked(defaultValue);
        swView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchCall.setSwitchListener(isChecked);
            }
        });

    }

    public interface SetSwitchCall {
        void setSwitchListener(boolean isChecked);
    }
}
