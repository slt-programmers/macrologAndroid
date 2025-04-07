package com.csl.macrologandroid.data.network;

import android.content.Context;

import com.csl.macrologandroid.BuildConfig;
import com.csl.macrologandroid.dtos.FoodDto;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public class FoodClient {

    private final ApiService apiService;
    private final Context context;

    public FoodClient(final Context context) {
        this.context = context;
        final var retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL)
                .client(new OkHttpClient.Builder().build())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public Observable<List<FoodDto>> getAllFood() {
        return apiService.getAlFood(getToken());
    }

    public Observable<ResponseBody> postFood(FoodDto food) {
        return apiService.postFood(getToken(), food);
    }

    private String getToken() {
        final var token = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE).getString("TOKEN", null);
        return "Bearer " + token;
    }

    private interface ApiService {

        @GET("food")
        Observable<List<FoodDto>> getAlFood(@Header("Authorization") String token);

        @POST("food")
        Observable<ResponseBody> postFood(@Header("Authorization") String token, @Body FoodDto food);

    }
}
