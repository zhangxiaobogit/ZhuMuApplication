package com.facecompare.zhumu;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.csht.common.LogUtils;
import com.facecompare.zhumu.common.GetUsersBean;
import com.facecompare.zhumu.common.UserListRequestBean;
import com.facecompare.zhumu.network.CshtNetWork;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class GetUseListService extends Service {
    ExecutorService checkUploadThreadExecutor;
    boolean checkFlag = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        checkUploadThreadExecutor = Executors.newCachedThreadPool();
        check();
    }

    private Disposable disposable;

    private void check() {
        checkUploadThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (checkFlag) {
                    try {
                        disposable = CshtNetWork.getCshtRequestPersonApi("http:///")
                                .getPersonl(new GetUsersBean(Build.SERIAL, ""))
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .subscribe(new Consumer<UserListRequestBean>() {
                                    @Override
                                    public void accept(UserListRequestBean userList) throws Exception {
                                        if (!disposable.isDisposed()) {
                                            disposable.dispose();
                                        }

                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        LogUtils.e("zxb", "  更新人员   " + throwable.getMessage());
                                        if (!disposable.isDisposed()) {
                                            disposable.dispose();
                                        }
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        checkFlag = false;
    }
}
