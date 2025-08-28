package com.csl.macrologandroid.services;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.repositories.ActivityRepository;
import com.csl.macrologandroid.data.repositories.DishRepository;
import com.csl.macrologandroid.data.repositories.FoodRepository;
import com.csl.macrologandroid.data.repositories.LogEntryRepository;
import com.csl.macrologandroid.data.repositories.UserSettingsRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;

public class SyncService {

    private final FoodRepository foodRepository;
    private final MutableLiveData<Boolean> foodSynced;
    private final DishRepository dishRepository;
    private final MutableLiveData<Boolean> dishesSynced;
    private final LogEntryRepository logEntryRepository;
    private final MutableLiveData<Boolean> logEntriesSynced;
    private final UserSettingsRepository userSettingsRepository;
    private final ActivityRepository activityRepository;
    private final MutableLiveData<Boolean> userSettingsSynced;
    private final MutableLiveData<Boolean> activitiesSynced;
    private final MutableLiveData<Boolean> mAllSynced = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> mSyncNeeded = new MutableLiveData<>();
    private final List<Disposable> disposables = new ArrayList<>();

    public SyncService(final Application application) {
        foodRepository = new FoodRepository(application);
        dishRepository = new DishRepository(application);
        logEntryRepository = new LogEntryRepository(application);
        userSettingsRepository = new UserSettingsRepository(application);
        activityRepository = new ActivityRepository(application);
        foodSynced = foodRepository.getMSynced();
        dishesSynced = dishRepository.getMSynced();
        logEntriesSynced = logEntryRepository.getMSynced();
        userSettingsSynced = userSettingsRepository.getMSynced();
        activitiesSynced = activityRepository.getMSynced();
    }

    public void checkSyncNeeded() {
        foodRepository.getMFood().observeForever(foods -> {
            if (foods == null || foods.isEmpty()) {
                mSyncNeeded.postValue(true);
            } else {
                mAllSynced.postValue(true);
            }
        });
        foodRepository.getFood();
    }

    public MutableLiveData<Boolean> syncNetworkWithLocalData() {
        mSyncNeeded.observeForever(needsSync -> {
            if (needsSync) {
                doSync();
            } else {
                mAllSynced.postValue(true);
            }
        });
        checkSyncNeeded();
        return mAllSynced;
    }

    public void disposeAll() {
        for (var disposable : disposables) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    private void doSync() {
        foodSynced.observeForever(synced -> {
            if (synced) disposables.add(dishRepository.getNetworkDishes());
        });
        dishesSynced.observeForever(synced -> {
            if (synced) disposables.addAll(logEntryRepository.getNetworkLogEntries(new Date()));
        });
        logEntriesSynced.observeForever(synced -> {
            if (synced) disposables.add(userSettingsRepository.getNetworkUserSettings());
        });
        userSettingsSynced.observeForever(synced -> {
            if (synced) disposables.addAll(activityRepository.getNetworkActivities(new Date()));
        });
        activitiesSynced.observeForever(synced -> {
            if (synced) mAllSynced.postValue(true);
        });
        disposables.add(foodRepository.getNetworkFood());
    }
}
