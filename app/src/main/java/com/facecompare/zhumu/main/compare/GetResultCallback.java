package com.facecompare.zhumu.main.compare;

import com.facecompare.zhumu.common.dbentity.VisitorInfo;

public interface GetResultCallback {
    void getCompareResultCall(VisitorInfo compareResult);
    void onFaceDismiss(int showTag);
}
