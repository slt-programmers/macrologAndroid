package com.csl.macrologandroid.data.network;

import android.content.Context;

import com.csl.macrologandroid.BuildConfig;
import com.csl.macrologandroid.dtos.LogEntryRequest;
import com.csl.macrologandroid.dtos.LogEntryResponse;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.util.DateUtil;

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
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class LogEntryClient {

    private final ApiService apiService;
    private final Context context;

    public LogEntryClient(final Context context) {
        this.context = context;
        final var client = new OkHttpClient.Builder();
        client.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        });
        final var retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL)
                .client(client.build())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public Observable<List<LogEntryResponse>> getLogsForDay(Date date) {
        return apiService.getLogsForDay(getToken(), DateUtil.format(date));
    }

    public Observable<List<LogEntryResponse>> postEntries(List<LogEntryRequest> entries, Date date, Meal meal) {
        return apiService.postEntries(getToken(), entries, DateUtil.format(date), meal);
    }

    private String getToken() {
        final var token = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE).getString("TOKEN", null);
        return "Bearer " + token;
    }

    private interface ApiService {

        @GET("logs/day/{date}")
        Observable<List<LogEntryResponse>> getLogsForDay(
                @Header("Authorization") String token,
                @Path("date") String date);

        @POST("logs/day/{date}/{meal}")
        Observable<List<LogEntryResponse>> postEntries(
                @Header("Authorization") String token,
                @Body List<LogEntryRequest> entries,
                @Path("date") String date,
                @Path("meal") Meal meal);

    }
}
