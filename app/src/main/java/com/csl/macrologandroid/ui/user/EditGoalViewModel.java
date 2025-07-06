package com.csl.macrologandroid.ui.user;

import android.app.Application;
import android.text.Editable;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.repositories.UserSettingsRepository;
import com.csl.macrologandroid.models.UserSettings;

import java.util.Optional;

import lombok.Getter;


public class EditGoalViewModel extends AndroidViewModel {

    private final UserSettingsRepository userSettingsRepository;
    @Getter
    private final MutableLiveData<UserSettings> mUserSettings;

    public EditGoalViewModel(@NonNull Application application) {
        super(application);
        userSettingsRepository = new UserSettingsRepository(application);
        mUserSettings = userSettingsRepository.getMUserSettings();
    }

    public void loadUserSettings() {
        userSettingsRepository.getUserSettings();
    }

    public long calculateCalories(final Editable proteinEditable, final Editable fatEditable, final Editable carbsEditable) {
        double protein = Double.parseDouble(handleEmptyString(proteinEditable));
        double fat = Double.parseDouble(handleEmptyString(fatEditable));
        double carbs = Double.parseDouble(handleEmptyString(carbsEditable));

        return Math.round((protein * 4.0) + (fat * 9.0) + (carbs * 4.0));
    }

    private String handleEmptyString(final Editable editable) {
        if (editable == null || editable.toString().isEmpty()) {
            return "0.0";
        }
        return editable.toString();
    }

    public Optional<String> saveGoalMacros(final Editable proteinEditable, final Editable fatEditable, final Editable carbsEditable) {
        if (proteinEditable == null || proteinEditable.toString().isEmpty() ||
        fatEditable == null || fatEditable.toString().isEmpty() || carbsEditable == null || carbsEditable.toString().isEmpty()) {
            return Optional.of("Not al fields are filled");
        }

        final var macros = UserSettings.builder()
                .goalProtein(Integer.parseInt(proteinEditable.toString()))
                .goalFat(Integer.parseInt(fatEditable.toString()))
                .goalCarbs(Integer.parseInt(carbsEditable.toString()))
                .build();
        userSettingsRepository.insertMacros(macros);
        return Optional.empty();
    }
}
