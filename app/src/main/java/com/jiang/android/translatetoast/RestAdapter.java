package com.jiang.android.translatetoast;

import com.jiang.android.translatetoast.model.TranslateModel;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by jiang on 5/20/16.
 */

public class RestAdapter {

    private static String baseUrl = "http://fanyi.youdao.com";

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(new OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static ApiService getApiService() {
        return retrofit.create(ApiService.class);
    }

    static interface ApiService {

        @GET("/openapi.do")
        Call<TranslateModel> translate(@Query("keyfrom") String keyfrom, @Query("key") String key, @Query("type") String type, @Query("doctype") String doctype, @Query("callback") String callback, @Query("version") String version, @Query("q") String q);
    }

}
