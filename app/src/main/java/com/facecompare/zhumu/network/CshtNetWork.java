package com.facecompare.zhumu.network;

import com.facecompare.zhumu.network.netapi.CshtRequestApi;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Administrator on 2019-6-6.
 */

public class CshtNetWork {
    private static Converter.Factory gsonConverterFactory = GsonConverterFactory.create();
    private static CallAdapter.Factory rxJavaCallAdapterFactory = RxJava2CallAdapterFactory.create();
    private static CshtRequestApi cshtRequestApi;
    public static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();

    public static CshtRequestApi getCshtRequestApi(String baseUrl) {
        if (cshtRequestApi == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(baseUrl)
                    .addConverterFactory(gsonConverterFactory)
                    .addCallAdapterFactory(rxJavaCallAdapterFactory)
                    .build();
            cshtRequestApi = retrofit.create(CshtRequestApi.class);
        }
        return cshtRequestApi;
    }

    public static CshtRequestApi getCshtRequestPersonApi(String baseUrl) {
        if (cshtRequestApi == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(baseUrl)
//                    .addConverterFactory(new NullOnEmptyConverterFactory())
                    .addConverterFactory(gsonConverterFactory)
                    .addCallAdapterFactory(rxJavaCallAdapterFactory)
                    .build();
            cshtRequestApi = retrofit.create(CshtRequestApi.class);
        }
        return cshtRequestApi;
    }


}
