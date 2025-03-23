package com.csl.macrologandroid.ui.login;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.csl.macrologandroid.ForgotPasswordActivity;
import com.csl.macrologandroid.R;
import com.csl.macrologandroid.RegisterActivity;
import com.csl.macrologandroid.RoutingActivity;
import com.csl.macrologandroid.databinding.ActivityLoginBinding;
import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.util.ResetErrorTextWatcher;

import java.net.ConnectException;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    // UI references.
    private EditText userEmailInput;
    private EditText passwordInput;
    private TextView userEmailError;
    private TextView passwordError;

    private LoginViewModel loginViewModel;

    private final ActivityResultLauncher<Intent> registerForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = new Intent();
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new LoginViewModel(this.getSharedPreferences("AUTH", MODE_PRIVATE));
        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult.isSuccess()) {
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                Log.e(this.getLocalClassName(), Objects.requireNonNull(loginResult.getError().getMessage()));
                if (loginResult.getError() instanceof ConnectException) {
                    passwordError.setText(R.string.connection_error);
                } else {
                    passwordError.setText(R.string.login_failed);
                }
                passwordError.setVisibility(View.VISIBLE);
            }
        });

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.hide();
        }

        userEmailInput = binding.userField;
        passwordInput = binding.passwordField;
        userEmailError = binding.userEmailError;
        passwordError = binding.passwordError;
        userEmailInput.addTextChangedListener(new ResetErrorTextWatcher(userEmailInput, userEmailError));
        passwordInput.addTextChangedListener(new ResetErrorTextWatcher(passwordInput, passwordError));

        TextView forgotPassword = binding.forgotPassword;
        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        Button mLoginButton = binding.loginButton;
        mLoginButton.setOnClickListener(view -> attemptLogin());

        TextView mRegisterText = binding.registerText;
        mRegisterText.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            registerForResult.launch(intent);
            finish();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Session.getInstance().isExpired()) {
            Intent intent = new Intent(this, RoutingActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Session.resetTimestamp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void attemptLogin() {
        String username = userEmailInput.getText().toString();
        String password = passwordInput.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            userEmailError.setVisibility(View.VISIBLE);
            userEmailInput.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            focusView = userEmailInput;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            passwordError.setText(R.string.error_field_required);
            passwordError.setVisibility(View.VISIBLE);
            passwordInput.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            if (focusView == null) {
                focusView = passwordInput;
            }
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            loginViewModel.authenticate(username, password);
        }
    }
}
