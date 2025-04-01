package com.csl.macrologandroid.data.network;

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
import retrofit2.http.POST;

public class DishClient {

    private final ApiService apiService;

    public DishClient(String token) {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL)
                .client(client.build())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public Observable<List<DishDto>> getAllDishes() {
        return apiService.getAllDishes();
    }

    public Observable<DishDto> postDish(DishDto dish) {
        return apiService.postDish(dish);
    }

    private interface ApiService {

        @GET("dishes")
        Observable<List<DishDto>> getAllDishes();

        @POST("dishes")
        Observable<DishDto> postDish(@Body DishDto dish);
    }
}
