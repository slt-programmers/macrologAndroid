package com.csl.macrologandroid.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.csl.macrologandroid.BuildConfig;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;

public class HealthcheckService extends Service {

    private final HealthcheckService.ApiService apiService;

    public HealthcheckService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(HealthcheckService.ApiService.class);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Observable<Boolean> healthcheck(String token) {
        if (token != null) {
            return apiService.healthcheck("Bearer " + token);
        } else {
            return apiService.healthcheck("");
        }
    }

    private interface ApiService {

        @GET("healthcheck")
        Observable<Boolean> healthcheck(@Header("Authorization") String authorization);

    }
}
