package com.csl.macrologandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.csl.macrologandroid.services.AuthenticationService;
import com.csl.macrologandroid.util.ResetErrorTextWatcher;

import io.reactivex.rxjava3.disposables.Disposable;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private TextView emailError;
    private Button sendButton;
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        emailInput = findViewById(R.id.email_edittext);
        emailError = findViewById(R.id.email_error);

        emailInput.addTextChangedListener(textWatcher);
        emailInput.addTextChangedListener(new ResetErrorTextWatcher(emailInput, emailError));

        sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(v -> attemptSend());
        sendButton.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void attemptSend() {
        Editable editable = emailInput.getText();
        if (editable != null) {
            String email = editable.toString();
            if (!email.contains("@")) {
                emailError.setVisibility(View.VISIBLE);
                emailInput.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            } else {
                AuthenticationService authService = new AuthenticationService(getToken());
                disposable = authService.resetPassword(email)
                        .subscribe(
                                res -> finish(),
                                err -> {
                                    if (err.getMessage() != null && err.getMessage().contains("404")) {
                                        emailError.setText(R.string.error_email_not_found);
                                    } else {
                                        emailError.setText(R.string.error_general);
                                    }
                                    emailError.setVisibility(View.VISIBLE);
                                    emailInput.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                                }
                        );
            }
        }
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not needed
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            sendButton.setEnabled(!(s == null || s.toString().isEmpty()));
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Not needed
        }
    };


    private String getToken() {
        return getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }
}
