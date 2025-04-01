package com.csl.macrologandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.services.HealthcheckService;

import java.net.SocketTimeoutException;
import java.util.Objects;

import io.reactivex.rxjava3.disposables.Disposable;


public class RoutingActivity extends AppCompatActivity {

    private HealthcheckService service;
    private String token;
    private int callCounter = 0;
    private Boolean expired;
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        service = new HealthcheckService();
        token = getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", null);

        Intent intent = getIntent();
        expired = (Boolean) intent.getSerializableExtra("SESSION_EXPIRED");
        Session.resetTimestamp();
        doHealthCheck();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private void doHealthCheck() {
        callCounter++;
        disposable = (service.healthcheck(token)).subscribe(
                        res -> {
                            if (expired == null) {
                                startActivity(new Intent(RoutingActivity.this, MainActivity.class));
                            }
                            finish();
                        },
                        err -> {
                            Log.e(this.getLocalClassName(), Objects.requireNonNull(err.getMessage()));
                            if (err instanceof SocketTimeoutException && callCounter < 4) {
                                Log.e(this.getLocalClassName(), "retry: " + callCounter);
                                doHealthCheck();
                            }
                            else {
                                Intent intent = new Intent(this, MainActivity.class);
                                intent.putExtra("TOKEN_EXPIRED", true);
                                Log.e(this.getLocalClassName(), "token expired");
                                startActivity(intent);
                                finish();
                            }
                        });

    }
}
