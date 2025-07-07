package com.csl.macrologandroid.ui.user;

import android.app.Application;
import android.text.Editable;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.repositories.UserSettingsRepository;
import com.csl.macrologandroid.models.Gender;
import com.csl.macrologandroid.models.UserSettings;

import java.util.Optional;

import lombok.Getter;

public class EditPersonalDetailsViewModel extends AndroidViewModel {

    public static final String DEFAULT_ACTIVITY = "1.375";

    private final UserSettingsRepository userSettingsRepository;
    @Getter
    private final MutableLiveData<UserSettings> mUserSettings;

    public EditPersonalDetailsViewModel(@NonNull final Application application) {
        super(application);
        userSettingsRepository = new UserSettingsRepository(application);
        mUserSettings = userSettingsRepository.getMUserSettings();
    }

    public void loadUserSettings() {
        userSettingsRepository.getUserSettings();
    }

    public Optional<String> saveUserSettings(final Editable nameEditable, final Editable birthdayEditable,
                                             final String gender, final Editable heightEditable, final Editable weightEditable,
                                             final String activity) {
        if (nameEditable == null || nameEditable.toString().isEmpty() ||
                birthdayEditable == null || birthdayEditable.toString().isEmpty() ||
                gender == null || gender.isEmpty() ||
                heightEditable == null || heightEditable.toString().isEmpty() ||
                weightEditable == null || weightEditable.toString().isEmpty() ||
                activity == null || activity.isEmpty()) {
            return Optional.of("Not all fields are filled");
        }
        final var settingsKandidate = UserSettings.builder()
                .name(nameEditable.toString())
                .birthday(birthdayEditable.toString()) // check format
                .gender(Gender.valueOf(gender.toUpperCase()))
                .height(Integer.parseInt(heightEditable.toString()))
                .currentWeight(Double.parseDouble(weightEditable.toString()))
                .activity(Double.parseDouble(mapActivity(activity)))
                .build();
        userSettingsRepository.insertUserSettings(settingsKandidate);

        return Optional.empty();
    }

    private String mapActivity(final String activityDescription) {
        return switch (activityDescription) {
            case "Sedentary" -> "1.2";
            case "Moderately active" -> "1.55";
            case "Very active" -> "1.725";
            case "Extremely active" -> "1.9";
            default -> DEFAULT_ACTIVITY;
        };
    }

}
