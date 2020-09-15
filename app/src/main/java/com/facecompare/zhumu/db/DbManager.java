package com.facecompare.zhumu.db;


import android.database.Cursor;

import com.csht.common.LogUtils;
import com.facecompare.zhumu.common.dbentity.Personnel;
import com.facecompare.zhumu.gen.PersonnelDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/23.
 */
public class DbManager {
    //eq相等 ne、neq不相等， gt大于， lt小于 gte、ge大于等于 lte、le 小于等于 not非 mod求模 等
    public static DbManager instance = null;

    private DbManager() {

    }
    public static synchronized final DbManager getInstance() {
        if (instance == null) {
            instance = new DbManager();
        }
        return instance;
    }



    public boolean isFeatureExist(String num) {

        QueryBuilder<Personnel> queryBuilder;
        Cursor cursor = null;
        try {
            PersonnelDao dao = GreenDaoManager.getInstance().getSession().getPersonnelDao();
            queryBuilder = dao.queryBuilder().where(PersonnelDao.Properties.IdNum.eq(num));
            cursor = queryBuilder.buildCursor().query();
            cursor.close();
            Personnel personnel = queryBuilder.build().unique();
            if (personnel.getFeature() == null || personnel.getFeature().length <= 0) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;

            }
        }
        return false;
    }


    public Personnel isPersonnelExist(String num) {

        QueryBuilder<Personnel> queryBuilder;
        Cursor cursor = null;
        try {
            PersonnelDao dao = GreenDaoManager.getInstance().getSession().getPersonnelDao();
            queryBuilder = dao.queryBuilder().where(PersonnelDao.Properties.IdNum.eq(num));
            cursor = queryBuilder.buildCursor().query();
            LogUtils.e("zxb", "run->   isPersonnelExist  courser 查询   \n");
            if (queryBuilder.list().size() > 0) {
                cursor.close();
                return queryBuilder.list().get(0);
            } else {
                cursor.close();
                return null;
            }
        } catch (Exception e) {
            LogUtils.e("zxb", "run-> isPersonnelExist  courser 异常       " + num + "      " + e.toString() + "    \n");
            if (cursor != null) {
                LogUtils.e("zxb", "run->  isPersonnelExist  courser 异常  关闭courser      " + num + "    \n");
                cursor.close();
                cursor = null;
            }
            return null;
        } finally {
            if (cursor != null) {
                LogUtils.e("zxb", " isPersonnelExist  courser 最终  关闭courser    \n");
                cursor.close();
                cursor = null;
            }
        }

    }


    public void insertPersonnel(Personnel personnel) {
        LogUtils.e("zxb", "run->  插入人员 + " + personnel.getIdName());
        try {
            PersonnelDao dao = GreenDaoManager.getInstance().getSession().getPersonnelDao();
            dao.insert(personnel);
        } catch (Exception e) {
            LogUtils.e("zxb", "run-> mqtt dberr" + e.toString());
        }

    }

    public void deletePersonnel(Personnel personnel) {
        PersonnelDao dao = GreenDaoManager.getInstance().getSession().getPersonnelDao();
        dao.delete(personnel);
    }

    public long queryPersonnelCountNum() {
        PersonnelDao dao = GreenDaoManager.getInstance().getSession().getPersonnelDao();
        return dao.count();
    }

    public void updatePersonnel(Personnel personnel) {
        PersonnelDao dao = GreenDaoManager.getInstance().getSession().getPersonnelDao();
        dao.update(personnel);
    }

    public Personnel selectPersonnel(String num) {
        PersonnelDao dao = GreenDaoManager.getInstance().getSession().getPersonnelDao();
        QueryBuilder<Personnel> qb = dao.queryBuilder();
        qb.where(PersonnelDao.Properties.IdNum.eq(num));
        if (qb.list().size() > 0) {
            return qb.list().get(0);
        } else {
            return null;
        }
    }

    public void deleteAllPersonnel() {
        PersonnelDao dao = GreenDaoManager.getInstance().getSession().getPersonnelDao();
        dao.deleteAll();
    }


    public List<Personnel> selectAllPersonnel() {
        QueryBuilder<Personnel> queryBuilder;
        Cursor cursor = null;
        try {
            PersonnelDao dao = GreenDaoManager.getInstance().getSession().getPersonnelDao();
            queryBuilder = dao.queryBuilder();
            cursor = queryBuilder.buildCursor().query();
//            LogUtils.e("zxb", "run->   selectAllPersonnel  courser 查询   \n");
            if (queryBuilder.list().size() > 0) {
                cursor.close();
                return queryBuilder.list();
            } else {
                cursor.close();
                return null;
            }
        } catch (Exception e) {
            if (cursor != null) {
//                LogUtils.e("zxb", "run->  selectAllPersonnel  courser 异常  关闭courser      " + e.toString() + "    \n");
                cursor.close();
                cursor = null;
            }
            return null;
        } finally {
            if (cursor != null) {
//                LogUtils.e("zxb", " selectAllPersonnel  courser 最终  关闭courser    \n");
                cursor.close();
                cursor = null;
            }
        }

    }

    public List<byte[]> selectAllFeatures() {
        PersonnelDao dao = GreenDaoManager.getInstance().getSession().getPersonnelDao();
        QueryBuilder<Personnel> qb = dao.queryBuilder();
        List<Personnel> personnelList = qb.list();
        List<byte[]> featureList = new ArrayList<>();
        for (Personnel p : personnelList) {
            featureList.add(p.getFeature());
        }
        return featureList;
    }

}
