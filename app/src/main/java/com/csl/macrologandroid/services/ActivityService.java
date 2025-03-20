package com.csl.macrologandroid.services;

import com.csl.macrologandroid.BuildConfig;
import com.csl.macrologandroid.dtos.ActivityRequest;
import com.csl.macrologandroid.dtos.ActivityResponse;
import com.csl.macrologandroid.util.DateParser;

import java.util.Date;
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
import retrofit2.http.Path;

public class ActivityService {

    private final ApiService apiService;

    public ActivityService(String token) {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Content-Type", "application/json")
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

    public Observable<List<ActivityResponse>> getActivitiesForDay(Date date) {
        return apiService.getActivitiesForDay(DateParser.format(date));
    }

    public Observable<List<ActivityResponse>> postActivitiesForDay(List<ActivityRequest> activities, Date date) {
        String day = DateParser.format(date);
        return apiService.postActivitiesForDay(day, activities);
    }

    private interface ApiService {

        @GET("activities/day/{date}?forceSync=true")
        Observable<List<ActivityResponse>> getActivitiesForDay(@Path("date") String date);

        @POST("activities/day/{date}")
        Observable<List<ActivityResponse>> postActivitiesForDay(@Path("date") String date, @Body List<ActivityRequest> entries);

    }
}
