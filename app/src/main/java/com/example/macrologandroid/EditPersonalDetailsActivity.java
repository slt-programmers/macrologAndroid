package com.example.macrologandroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.macrologandroid.dtos.UserSettingResponse;
import com.example.macrologandroid.lifecycle.Session;
import com.example.macrologandroid.models.Gender;
import com.example.macrologandroid.services.UserService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class EditPersonalDetailsActivity extends AppCompatActivity {

    private String originalName;
    private int originalAge;
    private Gender originalGender;
    private int originalHeight;
    private double originalWeight;
    private double originalActivity;

    private EditText editName;
    private EditText editAge;
    private RadioGroup genderRadios;
    private EditText editHeight;
    private EditText editWeight;
    private Spinner editActivity;

    private Button saveButton;

    private UserService userService = new UserService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_details);

        editName = findViewById(R.id.edit_name);
        editName.addTextChangedListener(textChangedListener);
        editAge = findViewById(R.id.edit_age);
        editAge.addTextChangedListener(textChangedListener);

        genderRadios = findViewById(R.id.radiogroup_gender);
        editHeight = findViewById(R.id.edit_height);
        editHeight.addTextChangedListener(textChangedListener);

        editWeight = findViewById(R.id.edit_weight);
        editWeight.addTextChangedListener(textChangedListener);

        Button backButton = findViewById(R.id.backbutton);
        saveButton = findViewById(R.id.savebutton);

        Intent intent = getIntent();
        boolean intake = intent.getBooleanExtra("INTAKE", false);
        if (intake) {
            genderRadios.check(R.id.check_male);
//            originalGender = Gender.MALE;
//            originalActivity = 1.2;
            backButton = findViewById(R.id.backbutton);
            backButton.setVisibility(View.GONE);
            TextView intakeTitle = findViewById(R.id.intake_title);
            intakeTitle.setVisibility(View.VISIBLE);
            saveButton.setEnabled(false);
        } else {
            originalName = intent.getStringExtra("name");
            editName.setText(originalName);

            originalAge = intent.getIntExtra("age", 0);
            editAge.setText(String.valueOf(originalAge));

            originalGender = (Gender) intent.getSerializableExtra("gender");
            if (Gender.FEMALE.equals(originalGender)) {
                genderRadios.check(R.id.check_female);
            } else {
                genderRadios.check(R.id.check_male);
            }

            originalHeight = intent.getIntExtra("height", 0);
            editHeight.setText(String.valueOf(originalHeight));

            originalWeight = intent.getDoubleExtra("weight", 0.0);
            editWeight.setText(String.valueOf(originalWeight));

            originalActivity = intent.getDoubleExtra("activity", 1.2);

            backButton.setOnClickListener(v -> finish());
        }
        setupSpinner();

        saveButton.setOnClickListener(v -> {
            saveSettings();
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Session.getInstance().resetTimestamp();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Session.getInstance().isExpired()) {
            Intent intent = new Intent(EditPersonalDetailsActivity.this, SplashscreenActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);        }
    }

    private void setupSpinner() {
        editActivity = findViewById(R.id.edit_activity);

        List<String> list = new ArrayList<>();
        list.add("Sedentary");
        list.add("Lightly active");
        list.add("Moderately active");
        list.add("Very active");
        list.add("Extremely active");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editActivity.setAdapter(dataAdapter);

        switch (String.valueOf(originalActivity)) {
            case "1.2": editActivity.setSelection(0); break;
            case "1.375": editActivity.setSelection(1); break;
            case "1.55": editActivity.setSelection(2); break;
            case "1.725": editActivity.setSelection(3); break;
            case "1.9": editActivity.setSelection(4); break;
            default: editActivity.setSelection(0);
        }
    }

    @SuppressLint("CheckResult")
    private void saveSettings() {
        List<Observable<ResponseBody>> obsList = new ArrayList<>();

        String newName = editName.getText().toString();
        if (!newName.isEmpty() && !newName.equals(originalName)) {
            obsList.add(userService.putSetting(new UserSettingResponse(1, "name", newName)));
        }

        String newAge = editAge.getText().toString();
        if (!newAge.isEmpty() && originalAge != (Integer.valueOf(newAge))) {
            obsList.add(userService.putSetting(new UserSettingResponse(1, "age", newAge)));
        }

        RadioButton selected = findViewById(genderRadios.getCheckedRadioButtonId());
        String newGender = selected.getText().toString().toUpperCase();
        if (originalGender == null || !newGender.equals(originalGender.toString())) {
            obsList.add(userService.putSetting(new UserSettingResponse(1, "gender", newGender)));
        }

        String newHeight = editHeight.getText().toString();
        if (!newHeight.isEmpty() && originalHeight != Integer.valueOf(newHeight)) {
            obsList.add(userService.putSetting(new UserSettingResponse(1, "height", newHeight)));
        }

        String newWeight = editWeight.getText().toString();
        if (!newWeight.isEmpty() && !String.valueOf(originalWeight).equals(newWeight)) {
            obsList.add(userService.putSetting(new UserSettingResponse(1, "weight", newWeight)));
        }

        String item = (String) editActivity.getSelectedItem();
        String newActivity;
        switch (item) {
            case "Sedentary": newActivity = "1.2"; break;
            case "Lightly active": newActivity = "1.375"; break;
            case "Moderately active": newActivity = "1.55"; break;
            case "Very active": newActivity = "1.725"; break;
            case "Extremely active": newActivity = "1.9"; break;
            default: newActivity = "1.375";
        }
        if (!String.valueOf(originalActivity).equals(newActivity)) {
            obsList.add(userService.putSetting(new UserSettingResponse(1, "activity", newActivity)));
        }

        Observable.zip(obsList, i -> i)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("RELOAD", true);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                    }, err -> Log.d("Macrolog", err.getMessage()));
    }

    private void checkEmptyTextViews() {
        boolean nameIsEmpty = editName.getText().toString().isEmpty();
        boolean ageIsEmpty = editAge.getText().toString().isEmpty();
        boolean heightIsEmpty = editHeight.getText().toString().isEmpty();
        boolean weightIsEmpty = editWeight.getText().toString().isEmpty();

        if (!nameIsEmpty && !ageIsEmpty && !heightIsEmpty && !weightIsEmpty) {
            saveButton.setEnabled(true);
        } else {
            saveButton.setEnabled(false);
        }
    }

    private TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkEmptyTextViews();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
