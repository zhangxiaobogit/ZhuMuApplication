package com.facecompare.zhumu.util;

import android.text.TextUtils;

import java.util.UUID;

/**
 * 描 述：
 * 作 者：zxb  2020-09-24 15:44
 * 修改描述： XXX
 * 修 改 人： XXX  2020-09-24 15:44
 * 修改版本： XXX
 */
public class NumUtils {
    public static String getUUID() {
        if (!TextUtils.isEmpty(UUID.randomUUID().toString())) {
            return UUID.randomUUID().toString().replaceAll("-", "");
        } else {
            return UUID.randomUUID().toString();
        }
    }
}
