package com.csl.macrologandroid.ui.login;

import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.csl.macrologandroid.dtos.AuthenticationResponse;
import com.csl.macrologandroid.models.LoginResult;
import com.csl.macrologandroid.services.AuthenticationService;

import io.reactivex.rxjava3.disposables.Disposable;


public class LoginViewModel extends ViewModel {

    private final AuthenticationService authService;
    private final SharedPreferences sharedPreferences;
    private final MutableLiveData<LoginResult> loginResult;
    private Disposable disposable;

    public LoginViewModel(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        this.authService = new AuthenticationService(getToken());
        loginResult = new MutableLiveData<>();
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void authenticate(String username, String password) {
        this.disposable = this.authService.authenticate(username, password).subscribe(res -> {
            saveCredentials(res);
            loginResult.postValue(new LoginResult(true, null));
            this.disposable.dispose();
        }, err -> {
            loginResult.postValue(new LoginResult(false, err));
            this.disposable.dispose();
        });
    }

    private void saveCredentials(AuthenticationResponse result) {
        this.sharedPreferences.edit()
                .putString("USER", result.getName())
                .putString("TOKEN", result.getToken())
                .apply();
    } 

    private String getToken() {
        return this.sharedPreferences.getString("TOKEN", "");
    }
}
