package com.csl.macrologandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.csl.macrologandroid.dtos.ConnectivityRequest;
import com.csl.macrologandroid.dtos.ConnectivityResponse;
import com.csl.macrologandroid.services.UserService;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

//import io.reactivex.disposables.Disposable;

public class ConnectivityActivity extends AppCompatActivity implements BitmapHandler {

//    private Disposable disposable;

    private long applicationId;

    private final String redirectUri = "https://www.macrolog.herokuapp.com/callback";

    private UserService userService;
    private LinearLayout notConnectedLayout;
    private LinearLayout connectedLayout;
    private TextView accessError;
    private TextView accountName;
    private ImageView accountImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectivity);

        Intent intent = getIntent();

        notConnectedLayout = findViewById(R.id.not_connected_layout);
        connectedLayout = findViewById(R.id.connected_layout);
        accessError = findViewById(R.id.access_warning);
        accountName = findViewById(R.id.account_name);
        accountImage = findViewById(R.id.account_image);

        userService = new UserService(getToken());

        handleRedirect(intent);
//        handleGetSetting();

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        ImageButton stravaButton = findViewById(R.id.strava_connect);
        stravaButton.setOnClickListener(v -> {
            Uri intentUri = Uri.parse("https://www.strava.com/oauth/mobile/authorize")
                    .buildUpon()
                    .appendQueryParameter("client_id", String.valueOf(applicationId))
                    .appendQueryParameter("redirect_uri", redirectUri)
                    .appendQueryParameter("response_type", "code")
                    .appendQueryParameter("approval_prompt", "force")
                    .appendQueryParameter("scope", "activity:read_all")
                    .appendQueryParameter("state", "STRAVACONNECT")
                    .build();

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, intentUri);
            startActivity(browserIntent);
        });

        Button stravaDisconnect = findViewById(R.id.strava_disconnect);
//        stravaDisconnect.setOnClickListener(v -> disposable = userService.deleteConnectivitySetting("STRAVA").subscribe(
//                res -> handleGetSetting(),
//                err -> Log.e(this.getLocalClassName(), err.getMessage())
//        ));
    }

//    private void handleGetSetting() {
//        disposable = userService.getConnectivitySetting("STRAVA").subscribe(
//                res -> {
//                    if (res.getSyncedAccountId() == 0L) {
//                        applicationId = res.getSyncedApplicationId();
//                        connectedLayout.setVisibility(View.GONE);
//                        notConnectedLayout.setVisibility(View.VISIBLE);
//                    } else {
//                        notConnectedLayout.setVisibility(View.GONE);
//                        setConnectedLayoutVisible(res);
//                    }
//                },
//                err -> Log.e(this.getLocalClassName(), err.getMessage())
//        );
//    }

    private void handleRedirect(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            String scope = data.getQueryParameter("scope");
            String code = data.getQueryParameter("code");
            if (scope != null) {
                List<String> scopeList = Arrays.asList(scope.split(","));
                if (scopeList.contains("read") && scopeList.contains("activity:read_all") && code != null) {
                    ConnectivityRequest request = new ConnectivityRequest("code", code);
//                    disposable = userService.postConnectivitySetting("STRAVA", request).subscribe(
//                            res -> {
//                                setConnectedLayoutVisible(res);
//                                notConnectedLayout.setVisibility(View.GONE);
//                                accessError.setVisibility(View.GONE);
//                            },
//                            err -> Log.e(this.getLocalClassName(), err.getMessage())
//                    );
                } else {
                    accessError.setVisibility(View.VISIBLE);
                }
            } else {
                accessError.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setConnectedLayoutVisible(ConnectivityResponse response) {
        accountName.setText(response.getAccountName());
        new LoadImageTask(this).execute(response.getImageUrl(), this.getLocalClassName());
        connectedLayout.setVisibility(View.VISIBLE);
    }

    private String getToken() {
        return getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (disposable != null) {
//            disposable.dispose();
//        }
    }

    @Override
    public void handle(Bitmap bitmap) {
        if (bitmap != null) {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getWidth() / 2f, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            accountImage.setImageBitmap(output);
        }
    }

    static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

        private final BitmapHandler bitmapHandler;

        LoadImageTask(BitmapHandler bitmapHandler) {
            this.bitmapHandler = bitmapHandler;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(strings[0]);
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (Exception ex) {
                Log.e(strings[1], ex.getMessage());
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            this.bitmapHandler.handle(bitmap);
        }
    }
}


interface BitmapHandler {
    void handle(Bitmap bitmap);
}