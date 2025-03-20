package com.csl.macrologandroid;

import android.content.Intent;
import android.graphics.Typeface;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.services.AuthenticationService;

import java.util.Objects;

import io.reactivex.rxjava3.disposables.Disposable;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText oldPasswordView;
    private TextInputEditText newPasswordView;
    private TextInputEditText confirmPasswordView;

    private TextView errorTextView;
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        oldPasswordView = findViewById(R.id.old_password);
        oldPasswordView.setTypeface(Typeface.DEFAULT);
        oldPasswordView.setTransformationMethod(new PasswordTransformationMethod());

        newPasswordView = findViewById(R.id.new_password);
        newPasswordView.setTypeface(Typeface.DEFAULT);
        newPasswordView.setTransformationMethod(new PasswordTransformationMethod());

        confirmPasswordView = findViewById(R.id.confirm_password);
        confirmPasswordView.setTypeface(Typeface.DEFAULT);
        confirmPasswordView.setTransformationMethod(new PasswordTransformationMethod());

        errorTextView = findViewById(R.id.error_text);
        errorTextView.setVisibility(View.GONE);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        Button changeButton = findViewById(R.id.change_button);
        changeButton.setOnClickListener(v -> changePassword());
    }

    @Override
    public void onPause() {
        super.onPause();
        Session.resetTimestamp();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Session.getInstance().isExpired()) {
            Intent intent = new Intent(ChangePasswordActivity.this, RoutingActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void changePassword() {
        errorTextView.setText("");
        errorTextView.setVisibility(View.GONE);

        String newPassword = Objects.requireNonNull(newPasswordView.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(confirmPasswordView.getText()).toString().trim();

        String oldPassword = Objects.requireNonNull(oldPasswordView.getText()).toString();
        if (newPassword.equals(confirmPassword)) {
            if (!oldPassword.equals(newPassword)) {
                AuthenticationService service = new AuthenticationService(getToken());
                disposable = service.changePassword(oldPassword, newPassword, confirmPassword)
                .subscribe(
                        res -> finish(),
                        err -> {
                            errorTextView.setText(R.string.old_password_incorrect);
                            errorTextView.setVisibility(View.VISIBLE);
                        });

            } else {
                errorTextView.setText(R.string.old_same_as_new);
                errorTextView.setVisibility(View.VISIBLE);
            }
        } else {
            errorTextView.setText(R.string.must_be_same_passwords);
            errorTextView.setVisibility(View.VISIBLE);
        }
    }

    private String getToken() {
        return getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }
}
