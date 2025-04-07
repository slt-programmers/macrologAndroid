package com.csl.macrologandroid.data.network;

import android.content.Context;

import com.csl.macrologandroid.BuildConfig;
import com.csl.macrologandroid.dtos.DishDto;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public class DishClient {

    private final ApiService apiService;
    private final Context context;

    public DishClient(final Context context) {
        this.context = context;
        final var retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL)
                .client(new OkHttpClient.Builder().build())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public Observable<List<DishDto>> getAllDishes() {
        return apiService.getAllDishes(getToken());
    }

    public Observable<DishDto> postDish(DishDto dish) {
        return apiService.postDish(getToken(), dish);
    }

    private String getToken() {
        final var token = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE).getString("TOKEN", null);
        return "Bearer " + token;
    }

    private interface ApiService {

        @GET("dishes")
        Observable<List<DishDto>> getAllDishes(@Header("Authorization") String token);

        @POST("dishes")
        Observable<DishDto> postDish(@Header("Authorization") String token, @Body DishDto dish);
    }
}
