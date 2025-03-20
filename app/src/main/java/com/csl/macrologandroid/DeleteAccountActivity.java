package com.csl.macrologandroid;

import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;

import com.csl.macrologandroid.cache.DiaryLogCache;
import com.csl.macrologandroid.cache.FoodCache;
import com.csl.macrologandroid.cache.UserSettingsCache;
import com.csl.macrologandroid.services.AuthenticationService;

import java.util.Objects;

import io.reactivex.rxjava3.disposables.Disposable;
import retrofit2.HttpException;

public class DeleteAccountActivity extends AppCompatActivity {

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        TextInputLayout passwordLayout = findViewById(R.id.password_layout);
        Objects.requireNonNull(passwordLayout.getEditText()).setTransformationMethod(new PasswordTransformationMethod());

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        Button deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> attemptDelete());
    }

    private void attemptDelete() {
        TextInputLayout passwordLayout = findViewById(R.id.password_layout);
        String password = Objects.requireNonNull(passwordLayout.getEditText()).getText().toString();

        AuthenticationService authService = new AuthenticationService(getToken());
        disposable = authService.deleteAccount(password)
                .subscribe(res -> {
                    UserSettingsCache.getInstance().clearCache();
                    FoodCache.getInstance().clearCache();
                    DiaryLogCache.getInstance().clearCache();
                    setResult(RESULT_OK);
                    finish();
                }, err -> {
                    if (err instanceof HttpException) {
                        passwordLayout.setErrorEnabled(true);
                        passwordLayout.setError("Password incorrect");
                    } else {
                        passwordLayout.setErrorEnabled(true);
                        passwordLayout.setError("Could not delete account");
                    }
                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private String getToken() {
        return getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }
}
