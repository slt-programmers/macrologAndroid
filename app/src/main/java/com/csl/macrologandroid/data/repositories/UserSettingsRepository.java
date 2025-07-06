package com.csl.macrologandroid.data.repositories;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.local.LocalDatabase;
import com.csl.macrologandroid.data.local.daos.UserSettingsDao;
import com.csl.macrologandroid.data.network.UserSettingsClient;
import com.csl.macrologandroid.dtos.UserSettingsResponse;
import com.csl.macrologandroid.mappers.UserSettingsMapper;
import com.csl.macrologandroid.models.UserSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.Getter;

public class UserSettingsRepository {

    private final UserSettingsDao userSettingsDao;
    private final UserSettingsClient userSettingsClient;
    @Getter
    private final MutableLiveData<Boolean> mSynced = new MutableLiveData<>(false);
    @Getter
    private final MutableLiveData<UserSettings> mUserSettings = new MutableLiveData<>();
    private final List<Disposable> disposables = new ArrayList<>();

    public UserSettingsRepository(final Application application) {
        final var db = LocalDatabase.getDatabase(application.getApplicationContext());
        userSettingsDao = db.userSettingsDao();
        userSettingsClient = new UserSettingsClient(application.getApplicationContext());
    }

    public void getUserSettings() {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            final var entity = userSettingsDao.get();
            final var model = UserSettingsMapper.mapEntityToModel(entity);
            mUserSettings.postValue(model);
        });

    }

    public void insertUserSettings(final UserSettingsResponse response) {
        final var entity = UserSettingsMapper.mapResponseToEntity(response);
        userSettingsDao.insert(entity);
    }

    public void getNetworkUserSettings() {
        disposables.add(userSettingsClient.getUserSettings().observeOn(AndroidSchedulers.mainThread()).subscribe(response ->
                LocalDatabase.databaseWriteExecutor.execute(() -> {
                    insertUserSettings(response);
                    mSynced.postValue(true);
                }), err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))));
    }
}
