package com.csl.macrologandroid;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.csl.macrologandroid.dtos.AuthenticationResponse;
import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.services.AuthenticationService;
import com.csl.macrologandroid.ui.login.LoginActivity;
import com.csl.macrologandroid.util.ResetErrorTextWatcher;

import java.util.Objects;

import io.reactivex.rxjava3.disposables.Disposable;
import retrofit2.HttpException;

public class RegisterActivity extends AppCompatActivity {

    // UI references.
    private EditText newUserInput;
    private EditText newEmailInput;
    private EditText newPasswordInput;
    private TextView userError;
    private TextView emailError;
    private TextView passwordError;

    private AuthenticationService authService;

    private Disposable disposable;

    private final ActivityResultLauncher<Intent> editPersonalDetailsForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            finishWithResult();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.hide();
        }

        newUserInput = findViewById(R.id.register_username);
        newEmailInput = findViewById(R.id.register_email);
        newPasswordInput = findViewById(R.id.register_password);
        userError = findViewById(R.id.user_error);
        emailError = findViewById(R.id.email_error);
        passwordError = findViewById(R.id.password_error);

        newUserInput.addTextChangedListener(new ResetErrorTextWatcher(newUserInput, userError));
        newEmailInput.addTextChangedListener(new ResetErrorTextWatcher(newEmailInput, emailError));
        newPasswordInput.addTextChangedListener(new ResetErrorTextWatcher(newPasswordInput, passwordError));

        Button mRegisterButton = findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(v -> attemptRegister());

        TextView loginText = findViewById(R.id.login_text);
        loginText.setOnClickListener(v -> toLogin());

        authService = new AuthenticationService(getToken());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Session.getInstance().isExpired()) {
            Intent intent = new Intent(RegisterActivity.this, RoutingActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);
        }
    }


    private void toLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity((intent));
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        Session.resetTimestamp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void attemptRegister() {
        String username = Objects.requireNonNull(newUserInput).getText().toString();
        String email = Objects.requireNonNull(newEmailInput).getText().toString();
        String password = Objects.requireNonNull(newPasswordInput).getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (isUsernameInvalid(username)) {
            newUserInput.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            userError.setVisibility(View.VISIBLE);
            focusView = newUserInput;
            cancel = true;
        }

        if (!isEmailValid(email)) {
            newEmailInput.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            emailError.setVisibility(View.VISIBLE);
            focusView = newEmailInput;
            cancel = true;
        }

        if (isPasswordInvalid(password)) {
            newPasswordInput.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            passwordError.setText(R.string.error_password_invalid);
            passwordError.setVisibility(View.VISIBLE);
            focusView = newPasswordInput;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            register(username, email, password);
        }
    }

    private boolean isUsernameInvalid(String username) {
        return TextUtils.isEmpty(username);
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordInvalid(String password) {
        return TextUtils.isEmpty(password) || password.length() < 6;
    }

    private void finishWithResult() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void register(String username, String email, String password) {
        disposable = authService.register(username, email, password).subscribe(res -> {
                    saveCredentials(res);
                    Intent intent = new Intent(this, EditPersonalDetailsActivity.class);
                    intent.putExtra("INTAKE", true);
                    editPersonalDetailsForResult.launch(intent);
                },
                err -> {
                    if (err instanceof HttpException && ((HttpException) err).response().errorBody() != null) {
                        passwordError.setText(Objects.requireNonNull(((HttpException) err).response().errorBody()).string());
                    } else {
                        passwordError.setText(R.string.general_error);
                    }
                    passwordError.setVisibility(View.VISIBLE);
                });
    }

    private void saveCredentials(AuthenticationResponse result) {
        getSharedPreferences("AUTH", MODE_PRIVATE)
                .edit()
                .putString("USER", result.getName())
                .putString("TOKEN", result.getToken())
                .apply();
    }

    private String getToken() {
        return getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }

}

