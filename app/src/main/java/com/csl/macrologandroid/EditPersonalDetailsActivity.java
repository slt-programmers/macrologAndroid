package com.csl.macrologandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

import com.csl.macrologandroid.cache.UserSettingsCache;
import com.csl.macrologandroid.dtos.SettingsResponse;
import com.csl.macrologandroid.dtos.UserSettingsResponse;
import com.csl.macrologandroid.lifecycle.Session;
import com.csl.macrologandroid.models.Gender;
import com.csl.macrologandroid.data.network.UserSettingsClient;
import com.csl.macrologandroid.util.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

import okhttp3.ResponseBody;

public class EditPersonalDetailsActivity extends AppCompatActivity {

    private static final String DEFAULT_ACTIVITY = "1.375";

    private String originalName;
    private Date originalBirthday;
    private Gender originalGender;
    private int originalHeight;
    private double originalWeight;
    private double originalActivity;

    private TextInputEditText editName;
    private TextInputEditText editBirthday;
    private RadioGroup genderRadios;
    private TextInputEditText editHeight;
    private TextInputEditText editWeight;
    private Spinner editActivity;

    private boolean intake;

    private Button saveButton;

    private Disposable disposable;
    private TextInputLayout editBirthdayLayout;

    @Override
    public void onBackPressed() {
        if (!intake) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_details);

        editName = findViewById(R.id.edit_name);
        editName.addTextChangedListener(textChangedListener);

        editBirthdayLayout = findViewById(R.id.edit_birthday_layout);
        editBirthday = findViewById(R.id.edit_birthday);
        editBirthday.addTextChangedListener(textChangedListener);

        genderRadios = findViewById(R.id.radiogroup_gender);
        editHeight = findViewById(R.id.edit_height);
        editHeight.addTextChangedListener(textChangedListener);

        editWeight = findViewById(R.id.edit_weight);
        editWeight.addTextChangedListener(textChangedListener);

        Button backButton = findViewById(R.id.back_button);
        saveButton = findViewById(R.id.savebutton);

        Intent intent = getIntent();
        intake = intent.getBooleanExtra("INTAKE", false);
        if (intake) {
            genderRadios.check(R.id.check_male);
            backButton = findViewById(R.id.back_button);
            backButton.setVisibility(View.GONE);
            TextView intakeTitle = findViewById(R.id.intake_title);
            intakeTitle.setVisibility(View.VISIBLE);
            saveButton.setEnabled(false);
        } else {
            UserSettingsResponse settings = UserSettingsCache.getInstance().getCache();
            originalName = settings.getName();
            editName.setText(originalName);

            originalBirthday = settings.getBirthday();
            editBirthday.setText(DateUtil.format(originalBirthday));

            originalGender = settings.getGender();
            if (Gender.FEMALE.equals(originalGender)) {
                genderRadios.check(R.id.check_female);
            } else {
                genderRadios.check(R.id.check_male);
            }

            originalHeight = settings.getHeight();
            editHeight.setText(String.valueOf(originalHeight));

            originalWeight = settings.getCurrentWeight();
            editWeight.setText(String.valueOf(originalWeight));

            originalActivity = settings.getActivity();

            backButton.setOnClickListener(v -> finish());
        }
        setupSpinner();

        saveButton.setOnClickListener(v -> saveSettings());
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
            Intent intent = new Intent(EditPersonalDetailsActivity.this, StartupActivity.class);
            intent.putExtra("SESSION_EXPIRED", true);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
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
            case DEFAULT_ACTIVITY:
                editActivity.setSelection(1);
                break;
            case "1.55":
                editActivity.setSelection(2);
                break;
            case "1.725":
                editActivity.setSelection(3);
                break;
            case "1.9":
                editActivity.setSelection(4);
                break;
            default:
                editActivity.setSelection(0);
        }
    }

    private final ActivityResultLauncher<Intent> adjustIntakeForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            });

    private void saveSettings() {
        String newBirthday = Objects.requireNonNull(editBirthday.getText()).toString();
        Date newDate = DateUtil.parse(newBirthday);
        if (newDate == null) {
            editBirthdayLayout.setError("Incorrect format");
        } else {
            UserSettingsResponse userSettings = new UserSettingsResponse();
            List<Observable<ResponseBody>> obsList = fillObsList(userSettings, newDate, newBirthday);

            disposable = Observable.zip(obsList, i -> i)
                    .subscribe(res -> {
                        if (intake) {
                            Intent intent = new Intent(this, AdjustIntakeActivity.class);
                            intent.putExtra("userSettings", userSettings);
                            intent.putExtra("INTAKE", true);
                            adjustIntakeForResult.launch(intent);
                        } else {
                            UserSettingsCache.getInstance().clearCache();
                            Intent resultIntent = new Intent();
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }
                    }, err -> Log.e(this.getLocalClassName(), Objects.requireNonNull(err.getMessage())));
        }
    }

    private List<Observable<ResponseBody>> fillObsList(UserSettingsResponse userSettings, Date newDate, String newBirthday) {
        UserSettingsClient userSettingsClient = new UserSettingsClient(getApplicationContext());

        List<Observable<ResponseBody>> obsList = new ArrayList<>();

        String newName = Objects.requireNonNull(editName.getText()).toString();
        if (!newName.isEmpty() && !newName.equals(originalName)) {
            obsList.add(userSettingsClient.putSetting(new SettingsResponse(null, "name", newName)));
            userSettings.setName(newName);
        }

        if (!newDate.equals(originalBirthday)) {
            Calendar now = Calendar.getInstance();
            Calendar birthDay = Calendar.getInstance();
            birthDay.setTimeInMillis(newDate.getTime());
            int age = now.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
            obsList.add(userSettingsClient.putSetting(new SettingsResponse(null, "birthday", newBirthday)));
            obsList.add(userSettingsClient.putSetting(new SettingsResponse(null, "age", String.valueOf(age))));
            userSettings.setBirthday(newDate);
            userSettings.setAge(age);
        }

        RadioButton selected = findViewById(genderRadios.getCheckedRadioButtonId());
        String newGender = selected.getText().toString().toUpperCase();
        if (originalGender == null || !newGender.equals(originalGender.toString())) {
            obsList.add(userSettingsClient.putSetting(new SettingsResponse(null, "gender", newGender)));
            userSettings.setGender(Gender.valueOf(newGender));
        }

        String newHeight = Objects.requireNonNull(editHeight.getText()).toString();
        if (!newHeight.isEmpty() && originalHeight != Integer.parseInt(newHeight)) {
            obsList.add(userSettingsClient.putSetting(new SettingsResponse(null, "height", newHeight)));
            userSettings.setHeight(Integer.parseInt(newHeight));
        }

        String newWeight = Objects.requireNonNull(editWeight.getText()).toString();
        if (!newWeight.isEmpty() && !String.valueOf(originalWeight).equals(newWeight)) {
            obsList.add(userSettingsClient.putSetting(new SettingsResponse(null, "weight", newWeight)));
            userSettings.setCurrentWeight(Double.parseDouble(newWeight));
        }

        String item = (String) editActivity.getSelectedItem();
        String newActivity = switch (item) {
            case "Sedentary" -> "1.2";
            case "Moderately active" -> "1.55";
            case "Very active" -> "1.725";
            case "Extremely active" -> "1.9";
            default -> DEFAULT_ACTIVITY;
        };

        if (!String.valueOf(originalActivity).equals(newActivity)) {
            obsList.add(userSettingsClient.putSetting(new SettingsResponse(null, "activity", newActivity)));
            userSettings.setActivity(Double.parseDouble(newActivity));
        }

        return obsList;
    }


    private String getToken() {
        return this.getApplicationContext().getSharedPreferences("AUTH", MODE_PRIVATE).getString("TOKEN", "");
    }

    private final TextWatcher textChangedListener = new TextWatcher() {
        private void checkEmptyTextViews() {
            boolean nameIsEmpty = Objects.requireNonNull(editName.getText()).toString().isEmpty();
            boolean birthdayIsEmpty = Objects.requireNonNull(editBirthday.getText()).toString().isEmpty();
            boolean heightIsEmpty = Objects.requireNonNull(editHeight.getText()).toString().isEmpty();
            boolean weightIsEmpty = Objects.requireNonNull(editWeight.getText()).toString().isEmpty();

            saveButton.setEnabled(!nameIsEmpty && !birthdayIsEmpty && !heightIsEmpty && !weightIsEmpty);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not needed
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkEmptyTextViews();
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Not needed
        }
    };
}
