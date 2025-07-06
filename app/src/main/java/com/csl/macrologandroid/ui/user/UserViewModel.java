package com.csl.macrologandroid.ui.user;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.repositories.UserSettingsRepository;
import com.csl.macrologandroid.models.UserSettings;

import lombok.Getter;

public class UserViewModel extends AndroidViewModel {

    private final UserSettingsRepository userSettingsRepository;

    @Getter
    private final MutableLiveData<UserSettings> mUserSettings;

    public UserViewModel(@NonNull final Application application) {
        super(application);
        userSettingsRepository = new UserSettingsRepository(application);
        mUserSettings = userSettingsRepository.getMUserSettings();
    }

    public void loadUserSettings() {
        userSettingsRepository.getUserSettings();
    }
}
