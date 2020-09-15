package com.facecompare.zhumu.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.facecompare.zhumu.gen.DaoMaster;

import org.greenrobot.greendao.database.Database;

/**
 * Created by Administrator on 2016/10/31.
 */
public class MyOpenHelper extends DaoMaster.OpenHelper {

    public MyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {

        //super.onUpgrade(db, oldVersion, newVersion);
        if (oldVersion < newVersion) {
            DaoMaster.dropAllTables(db,true);
            DaoMaster.createAllTables(db,true);
//            LogUtils.i("lbw","===data up");
//            MigrationHelper.migrate(db, VisitorInfoDao.class);
//            MigrationHelper.migrate(db, PersonnelDao.class);
            //创建新表
//            GreenDaoDatabaseUtil.createTable(db, IcCardInfoDao.class);
//
//
//            //表中增加字段
//            GreenDaoDatabaseUtil.updateTableCols(db, VisitorInfoDao.class);
//            GreenDaoDatabaseUtil.updateTableCols(db, PersonnelDao.class);
//            GreenDaoDatabaseUtil.updateTableCols(db, IcCardInfoDao.class);


        }
    }
}
