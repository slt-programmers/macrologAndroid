package com.csl.macrologandroid.services;

import com.csl.macrologandroid.BuildConfig;
import com.csl.macrologandroid.dtos.AuthenticationRequest;
import com.csl.macrologandroid.dtos.AuthenticationResponse;
import com.csl.macrologandroid.dtos.ChangePasswordRequest;

import android.util.Base64;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class AuthenticationService {

    private final ApiService apiService;

    private final ApiService apiServiceWithBearer;

    public AuthenticationService(String token) {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        });

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL + "api/")
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create());

        apiService = builder.build().create(ApiService.class);
        apiServiceWithBearer = builder.client(client.build()).build().create(ApiService.class);
    }

    // The username field is used for both username and email when logging in
    // This is handled properly by the backend
    public Observable<AuthenticationResponse> authenticate(String username, String password) {
        return apiService.authenticate(new AuthenticationRequest(username, "", password));
    }

    public Observable<AuthenticationResponse> register(String username, String email, String password) {
        return apiService.register((new AuthenticationRequest(username, email, password)));
    }

    public Observable<ResponseBody> changePassword(String oldPassword, String newPassword, String confirmNew) {
        return apiServiceWithBearer.changePassword(new ChangePasswordRequest(oldPassword, newPassword, confirmNew));
    }

    public Observable<ResponseBody> resetPassword(String email) {
        return apiService.resetPassword(new AuthenticationRequest(null, email, null));
    }

    public Observable<ResponseBody> deleteAccount(String password) {
        String encryptedPassword = Base64.encodeToString(password.getBytes(), Base64.DEFAULT);
        return apiServiceWithBearer.deleteAccount(encryptedPassword);
    }

    private interface ApiService {

        @POST("authenticate")
        Observable<AuthenticationResponse> authenticate(@Body AuthenticationRequest request);

        @POST("signup")
        Observable<AuthenticationResponse> register(@Body AuthenticationRequest request);

        @POST("changePassword")
        Observable<ResponseBody> changePassword(@Body ChangePasswordRequest request);

        @POST("resetPassword")
        Observable<ResponseBody> resetPassword(@Body AuthenticationRequest email);

        @POST("deleteAccount")
        Observable<ResponseBody> deleteAccount(@Query("password") String password);

    }
}
