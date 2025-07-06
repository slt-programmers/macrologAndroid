package com.csl.macrologandroid.data.network;

import android.content.Context;

import com.csl.macrologandroid.BuildConfig;
import com.csl.macrologandroid.dtos.ConnectivityRequest;
import com.csl.macrologandroid.dtos.ConnectivityResponse;
import com.csl.macrologandroid.dtos.SettingsResponse;
import com.csl.macrologandroid.dtos.UserSettingsResponse;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import okhttp3.ResponseBody;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class UserSettingsClient {

    private final ApiService apiService;

    private final Context context;

    public UserSettingsClient(final Context context) {
        this.context = context;
        final var retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL)
                .client(new OkHttpClient.Builder().build())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public Observable<UserSettingsResponse> getUserSettings() {
        return apiService.getUserSettings(getToken());
    }

    public Observable<ResponseBody> putSetting(final SettingsResponse setting) {
        return apiService.putSetting(getToken(), setting);
    }

    public Observable<ConnectivityResponse> getConnectivitySetting(final String key) {
        return apiService.getConnectivitySetting(getToken(), key);
    }

    public Observable<ConnectivityResponse> postConnectivitySetting(final String platform, final ConnectivityRequest request) {
        return apiService.postConnectivitySetting(getToken(), platform, request);
    }

    public Observable<ResponseBody> deleteConnectivitySetting(final String platform) {
        return apiService.deleteConnectivitySetting(getToken(), platform);
    }

    private String getToken() {
        final var token = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE).getString("TOKEN", null);
        return "Bearer " + token;
    }

    private interface ApiService {

        @GET("settings/user")
        Observable<UserSettingsResponse> getUserSettings(@Header("Authorization") final String token
        );

        @PUT("settings")
        Observable<ResponseBody> putSetting(@Header("Authorization") final String token,
                                            @Body SettingsResponse setting);

        @GET("settings/connectivity/{key}")
        Observable<ConnectivityResponse> getConnectivitySetting(@Header("Authorization") final String token,
                                                                @Path("key") final String key);

        @POST("settings/connectivity/{platform}")
        Observable<ConnectivityResponse> postConnectivitySetting(@Header("Authorization") final String token,
                                                                 @Path("platform") final String platform,
                                                                 @Body final ConnectivityRequest request);

        @DELETE("settings/connectivity/{platform}")
        Observable<ResponseBody> deleteConnectivitySetting(@Header("Authorization") final String token,
                                                           @Path("platform") final String platform);
    }
}
