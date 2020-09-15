package com.facecompare.zhumu.db;


import com.facecompare.zhumu.gen.DaoMaster;
import com.facecompare.zhumu.gen.DaoSession;
import com.facecompare.zhumu.main.ZhumuApplication;

import org.greenrobot.greendao.identityscope.IdentityScopeType;

/**
 * Created by hasee on 2016-08-29.
 */
public class GreenDaoManager {
    private static GreenDaoManager mInstance;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;


    public GreenDaoManager() {
        MyOpenHelper openHelper = new MyOpenHelper(ZhumuApplication.getContextObject(), "zhumu.db", null);
        mDaoMaster = new DaoMaster(openHelper.getWritableDatabase());
//        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(MyApplication.getContextObject(), "evisitor-db", null);
//        mDaoMaster = new DaoMaster(devOpenHelper.getWritableDatabase());
        mDaoSession = mDaoMaster.newSession(IdentityScopeType.None);
    }

    public static GreenDaoManager getInstance() {
        if (mInstance == null) {
            mInstance = new GreenDaoManager();
        }
        return mInstance;
    }

    public DaoMaster getMaster() {
        return mDaoMaster;
    }

    public DaoSession getSession() {
        return mDaoSession;
    }

    public DaoSession getNewSession() {
        mDaoSession = mDaoMaster.newSession();
        return mDaoSession;
    }
}
