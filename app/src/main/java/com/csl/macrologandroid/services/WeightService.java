package com.csl.macrologandroid.services;

import com.csl.macrologandroid.BuildConfig;
import com.csl.macrologandroid.data.network.CustomGsonConverter;
import com.csl.macrologandroid.dtos.WeightRequest;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public class WeightService {

    private final ApiService apiService;

    public WeightService(String token) {
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
                .addConverterFactory(CustomGsonConverter.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public Observable<List<WeightRequest>> getAllMeasurements() {
        return apiService.getAllMeasurements();
    }

    public Observable<ResponseBody> postMeasurement(WeightRequest weightRequest) {
        return apiService.postMeasurement(weightRequest);
    }

    private interface ApiService {

        @GET("weight")
        Observable<List<WeightRequest>> getAllMeasurements();

        @POST("weight")
        Observable<ResponseBody> postMeasurement(@Body WeightRequest weightRequest);

    }
}
