package com.facecompare.zhumu.network.netapi;


import com.facecompare.zhumu.common.GetUsersBean;
import com.facecompare.zhumu.common.UserListRequestBean;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by Administrator on 2019-6-6.
 */

public interface CshtRequestApi {
    @POST("Visitor/webapi/info/getUsers")
    Observable<UserListRequestBean> getPersonl(@Body GetUsersBean getUsersBean);

    @GET  //{fileName}是动态码
    @Streaming
        //GET下载文件必须结合@Streaming使用
    Observable<ResponseBody> downLoadImg(@Url String fileName);

//    @POST("Visitor/webapi/employee/doorRegist")
//    Observable<RequestReultBean> uploadRulePersons(@Body JKDoorBean jkDoorBean);
//
//
//    @POST("Visitor/webapi/visit/register")
//    Observable<RequestReultBean> uploadPersonData(@Body VisitorRegInfo cshtBean);
//
//    @POST("Visitor/webapi/visit/registeronline")
//    Observable<RequestReultBean> uploadPersonOnlineData(@Body VisitorRegInfo cshtBean);
//
//    @POST("/form")
//    @FormUrlEncoded
//    Call<ResponseBody> testFormUrlEncoded2(@FieldMap Map<String, Object> map);
}
