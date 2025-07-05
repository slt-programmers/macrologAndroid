package com.csl.macrologandroid.services;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.repositories.DishRepository;
import com.csl.macrologandroid.data.repositories.FoodRepository;
import com.csl.macrologandroid.data.repositories.LogEntryRepository;

import java.util.Date;

public class SyncService {

    private final FoodRepository foodRepository;
    private final MutableLiveData<Boolean> foodSynced;
    private final DishRepository dishRepository;
    private final MutableLiveData<Boolean> dishesSynced;
    private final LogEntryRepository logEntryRepository;
    private final MutableLiveData<Boolean> logEntriesSynced;
    private final MutableLiveData<Boolean> mAllSynced = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> mSyncNeeded = new MutableLiveData<>();

    public SyncService(final Application application) {
        foodRepository = new FoodRepository(application);
        dishRepository = new DishRepository(application);
        logEntryRepository = new LogEntryRepository(application);
        foodSynced = foodRepository.getMSynced();
        dishesSynced = dishRepository.getMSynced();
        logEntriesSynced = logEntryRepository.getMSynced();
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

    private void doSync() {
        foodSynced.observeForever(synced -> {
            if (synced) dishRepository.getNetworkDishes();
        });
        dishesSynced.observeForever(synced -> {
            if (synced) logEntryRepository.getNetworkLogEntries(new Date());
        });
        logEntriesSynced.observeForever(synced -> {
            if (synced) mAllSynced.postValue(true);
        });
        foodRepository.getNetworkFood();
    }
}
