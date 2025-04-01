package com.csl.macrologandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.csl.macrologandroid.dtos.ActivityRequest;
import com.csl.macrologandroid.dtos.ActivityResponse;
import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.services.ActivityService;
import com.csl.macrologandroid.util.KeyboardManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.rxjava3.disposables.Disposable;

public class ActivityActivity extends AppCompatActivity {

    private Date selectedDate;
    private ActivityService activityService;

    private TextInputEditText editName;
    private TextInputEditText editCalories;

    private LinearLayout activityLayout;
    private Button saveButton;
    private Disposable postDisposable;
    private List<ActivityResponse> activities;
    private Button addButton;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (postDisposable != null) {
            postDisposable.dispose();
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
        if (acts != null) {
            for (Object act : acts) {
                if (act instanceof ActivityResponse) {
                    activities.add((ActivityResponse) act);
                }
            }
        }

        editName = findViewById(R.id.edit_name);
        editName.addTextChangedListener(textWatcher);

        editCalories = findViewById(R.id.edit_calories);
        editCalories.addTextChangedListener(textWatcher);

        activityLayout = findViewById(R.id.activity_layout);
        fillActivityLayout();

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
            KeyboardManager.hideKeyboard(this);
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
            Intent intent = new Intent(ActivityActivity.this, StartupActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);
        }
    }

    private void fillActivityLayout() {
        for (ActivityResponse act : activities) {
            addActivityToLayout(act);
        }
    }

    private void addActivity() {
        ActivityResponse act = new ActivityResponse();
        act.setDay(selectedDate);
        act.setName(Objects.requireNonNull(editName.getText()).toString().trim());
        act.setCalories(Integer.parseInt(Objects.requireNonNull(editCalories.getText()).toString().trim()));

        activities.add(act);
        addActivityToLayout(act);

        editName.setText("");
        editCalories.setText("");
        saveButton.setVisibility(View.VISIBLE);
    }

    private void addActivityToLayout(ActivityResponse act) {
        @SuppressLint("InflateParams")
        ConstraintLayout activityConstraintLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.layout_edit_activity, null);

        TextInputEditText activityName = activityConstraintLayout.findViewById(R.id.activity_name);
        activityName.setText(act.getName());

        ImageView trashImageView = activityConstraintLayout.findViewById(R.id.trash_icon);
        trashImageView.setOnClickListener(v -> removeActivity(act));

        TextInputEditText caloriesAmount = activityConstraintLayout.findViewById(R.id.calories_amount);
        caloriesAmount.setText(String.valueOf(act.getCalories()));

        activityLayout.addView(activityConstraintLayout);
    }

    private void removeActivity(ActivityResponse act) {
        int index = activities.indexOf(act);
        ConstraintLayout activityConstraintLayout = (ConstraintLayout) activityLayout.getChildAt(index);
        activityLayout.removeView((activityConstraintLayout));
        activities.remove(act);
    }

    private void saveActivities() {
        List<ActivityRequest> activityRequests = new ArrayList<>();

        for (ActivityResponse act : activities) {
            ActivityRequest request = makeActivityRequest(act);
            activityRequests.add(request);
        }

        postDisposable = activityService.postActivitiesForDay(activityRequests, selectedDate)
                .subscribe(res -> {
                    Intent resultIntent = new Intent();
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                });

    }

    private ActivityRequest makeActivityRequest(ActivityResponse act) {
        int index = activities.indexOf(act);
        ConstraintLayout editActivity = (ConstraintLayout) activityLayout.getChildAt(index);
        TextInputLayout activityNameLayout = (TextInputLayout) editActivity.getChildAt(0);
        String name = Objects.requireNonNull(activityNameLayout.getEditText()).getText().toString();

        TextInputLayout calorieLayout = (TextInputLayout) editActivity.getChildAt(4);
        int calories = Integer.parseInt(Objects.requireNonNull(calorieLayout.getEditText()).getText().toString());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        return new ActivityRequest(
                act.getId(),
                format.format(act.getDay()),
                name,
                calories,
                act.getSyncedWith(),
                act.getSyncedId()
        );
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
