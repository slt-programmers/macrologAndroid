package com.csl.macrologandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.csl.macrologandroid.dtos.ActivityRequest;
import com.csl.macrologandroid.dtos.ActivityResponse;
import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.services.ActivityService;
import com.csl.macrologandroid.util.DateParser;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.disposables.Disposable;

public class EditActivityActivity extends AppCompatActivity {

    private Date selectedDate;
    private ActivityService activityService;

    private TextInputEditText editName;
    private TextInputEditText editCalories;

    private LinearLayout activityLayout;
    private Button saveButton;
    private Disposable postDisposable;
    private Disposable deleteDisposable;
    private List<ActivityResponse> activities;
    private List<ActivityResponse> copyActs;
    private List<ActivityResponse> newActs;
    private Button addButton;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (postDisposable != null) {
            postDisposable.dispose();
        }
        if (deleteDisposable != null) {
            deleteDisposable.dispose();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_activity);

        selectedDate = (Date) getIntent().getSerializableExtra("DATE");
        activityService = new ActivityService(getToken());

        activities = new ArrayList<>();
        List acts = (List) getIntent().getSerializableExtra("ACTIVITIES");
        for (Object act : acts) {
            if (act instanceof ActivityResponse) {
                activities.add((ActivityResponse) act);
            }
        }

        copyActs = new ArrayList<>(activities);

        editName = findViewById(R.id.edit_name);
        editName.addTextChangedListener(textWatcher);

        editCalories = findViewById(R.id.edit_calories);
        editCalories.addTextChangedListener(textWatcher);

        activityLayout = findViewById(R.id.activity_layout);
        fillActivitylayout();

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            saveButton.setEnabled(false);
            saveActivities();
        });
        if (activities.isEmpty()) {
            saveButton.setVisibility(View.GONE);
        }

        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            hideSoftKeyboard();
            addActivity();
        });
        addButton.setEnabled(false);
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
            Intent intent = new Intent(EditActivityActivity.this, SplashscreenActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);
        }
    }

    private void fillActivitylayout() {
        for (ActivityResponse act : activities) {
            addActivityToLayout(act);
        }
    }

    private void appendNewActivity() {
        activities.addAll(newActs);
        copyActs.addAll(newActs);
        addActivityToLayout(newActs.get(0));
        saveButton.setVisibility(View.VISIBLE);
    }

    private void addActivity() {
        ActivityRequest act = new ActivityRequest(
                null,
                DateParser.format(selectedDate),
                Objects.requireNonNull(editName.getText()).toString().trim(),
                Integer.valueOf(Objects.requireNonNull(editCalories.getText()).toString().trim()),
                null,
                null);
        System.out.println(act);
        List<ActivityRequest> activityList = new ArrayList<>();
        activityList.add(act);
        postDisposable = activityService.postActivity(activityList)
                .subscribe(res -> {
                            newActs = res;
                            editName.setText("");
                            editCalories.setText("");
                            appendNewActivity();
                        },
                        err -> Log.e(this.getLocalClassName(), err.getMessage()));
    }

    private void addActivityToLayout(ActivityResponse act) {
        @SuppressLint("InflateParams")
        ConstraintLayout activityConstraintLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.layout_edit_activity, null);

        TextInputEditText activityName = activityConstraintLayout.findViewById(R.id.activity_name);
        activityName.setText(act.getName());

        ImageView trashImageView = activityConstraintLayout.findViewById(R.id.trash_icon);
        trashImageView.setOnClickListener(v -> toggleToRemoveActivity(act));

        TextInputEditText caloriesAmount = activityConstraintLayout.findViewById(R.id.calories_amount);
        caloriesAmount.setText(String.valueOf(act.getCalories()));

        activityLayout.addView(activityConstraintLayout);
    }

    private void toggleToRemoveActivity(ActivityResponse act) {
        int index = activities.indexOf(act);

        ConstraintLayout activityConstraintLayout = (ConstraintLayout) activityLayout.getChildAt(index);
        TextInputLayout nameLayout = (TextInputLayout) activityConstraintLayout.getChildAt(0);
        TextView calorieLabel = (TextView) activityConstraintLayout.getChildAt(1);
        ImageView trashcan = (ImageView) activityConstraintLayout.getChildAt(2);
        TextInputLayout caloriesAmountLayout = (TextInputLayout) activityConstraintLayout.getChildAt(4);

        if (copyActs.indexOf(act) == -1) {
            // item was removed, so add it again
            if (index > copyActs.size()) {
                copyActs.add(act);
            } else {
                copyActs.add(index, act);
            }
            nameLayout.setAlpha(1f);
            nameLayout.setEnabled(true);
            calorieLabel.setAlpha(1f);
            caloriesAmountLayout.setAlpha(1f);
            caloriesAmountLayout.setEnabled(true);
            trashcan.setImageResource(R.drawable.trashcan);
        } else {
            copyActs.remove(act);
            nameLayout.setAlpha(0.4f);
            nameLayout.setEnabled(false);
            calorieLabel.setAlpha(0.4f);
            caloriesAmountLayout.setAlpha(0.4f);
            caloriesAmountLayout.setEnabled(false);
            trashcan.setImageResource(R.drawable.replay);
        }
    }

    private void saveActivities() {
        List<ActivityRequest> activityRequests = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (ActivityResponse act : activities) {
            if (copyActs.indexOf(act) == -1) {
                deleteDisposable = activityService.deleteActivity(act.getId())
                        .subscribe(res -> {},
                                err -> Log.e(this.getLocalClassName(), err.getMessage()));
            } else {
                ActivityRequest request = makeActivityRequest(act);
                activityRequests.add(request);
            }
        }

        if (!activityRequests.isEmpty()) {
            postDisposable = activityService.postActivity(activityRequests)
                    .subscribe(res -> {
                        Intent resultIntent = new Intent();
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    });
        } else {
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }

    private ActivityRequest makeActivityRequest(ActivityResponse act) {
        int index = activities.indexOf(act);
        ConstraintLayout editActivity = (ConstraintLayout) activityLayout.getChildAt(index);
        TextInputLayout activityNameLayout = (TextInputLayout) editActivity.getChildAt(0);
        String name = Objects.requireNonNull(activityNameLayout.getEditText()).getText().toString();

        TextInputLayout calorieLayout = (TextInputLayout) editActivity.getChildAt(4);
        int calories = Integer.valueOf(Objects.requireNonNull(calorieLayout.getEditText()).getText().toString());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        return new ActivityRequest(
                (long) act.getId(),
                format.format(act.getDay()),
                name,
                calories,
                act.getSyncedWith(),
                act.getSyncedId()
        );
    }

    private void hideSoftKeyboard() {
        View view = findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not needed
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            addButton.setEnabled(!(s == null || s.toString().isEmpty()));
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