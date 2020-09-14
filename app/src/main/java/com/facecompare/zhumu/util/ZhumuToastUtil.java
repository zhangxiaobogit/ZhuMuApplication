package com.facecompare.zhumu.util;

import android.app.Activity;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facecompare.zhumu.R;
import com.facecompare.zhumu.main.ZhumuApplication;


/**
 * Created by Administrator on 2019-6-12.
 */

public class ZhumuToastUtil {
    public static Toast noCarPassToast;


    public static void showNoCarPassToast(Activity context, String msg, int time) {
        if (noCarPassToast == null) {
            noCarPassToast = new Toast(context);
        }
        noCarPassToast.setDuration(Toast.LENGTH_LONG);
        noCarPassToast.setGravity(Gravity.CENTER, 0, 0);
        LayoutInflater inflater = context.getLayoutInflater();
        LinearLayout toastLayout = (LinearLayout) inflater.inflate(R.layout.toast_csht_self, null);
        TextView txtToast = toastLayout.findViewById(R.id.txt_toast);
        txtToast.setText(msg);
        noCarPassToast.setView(toastLayout);
        noCarPassToast.show();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                noCarPassToast.cancel();
            }
        }, time);
    }

    public static void showToast(String str) {
        Toast.makeText(ZhumuApplication.getContext(), str, Toast.LENGTH_LONG).show();
    }
}
