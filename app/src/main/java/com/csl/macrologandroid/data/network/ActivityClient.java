package com.csl.macrologandroid.data.network;

import android.content.Context;

import com.csl.macrologandroid.BuildConfig;
import com.csl.macrologandroid.dtos.ActivityRequest;
import com.csl.macrologandroid.dtos.ActivityResponse;
import com.csl.macrologandroid.util.DateUtil;

import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class ActivityClient {

    private final ActivityClient.ApiService apiService;
    private final Context context;

    public ActivityClient(final Context context) {
        this.context = context;
        final var retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL)
                .client(new OkHttpClient.Builder().build())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public Observable<List<ActivityResponse>> getActivitiesForDay(final Date date) {
        return apiService.getActivitiesForDay(getToken(), DateUtil.format(date));
    }

    public Observable<List<ActivityResponse>> postActivitiesForDay(final List<ActivityRequest> activities,
                                                                   final Date date) {
        String day = DateUtil.format(date);
        return apiService.postActivitiesForDay(getToken(), day, activities);
    }

    private String getToken() {
        final var token = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE).getString("TOKEN", null);
        return "Bearer " + token;
    }

    private interface ApiService {

        @GET("activities/day/{date}?forceSync=true")
        Observable<List<ActivityResponse>> getActivitiesForDay(@Header("Authorization") String token, @Path("date") String date);

        @POST("activities/day/{date}")
        Observable<List<ActivityResponse>> postActivitiesForDay(@Header("Authorization") String token,
                                                                @Path("date") String date, @Body List<ActivityRequest> entries);

    }
}
