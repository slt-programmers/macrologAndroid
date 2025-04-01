package com.csl.macrologandroid.data.repositories;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.local.LocalDatabase;
import com.csl.macrologandroid.data.local.daos.FoodDao;
import com.csl.macrologandroid.data.local.daos.LogEntryDao;
import com.csl.macrologandroid.data.models.LogEntryData;
import com.csl.macrologandroid.dtos.FoodDto;
import com.csl.macrologandroid.dtos.LogEntryResponse;
import com.csl.macrologandroid.mappers.LogEntryMapper;
import com.csl.macrologandroid.models.LogEntry;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.data.network.LogEntryClient;
import com.csl.macrologandroid.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.Getter;

public class LogEntryRepository {

    private final LogEntryDao logEntryDao;
    private final FoodDao foodDao;
    private final LogEntryClient logEntryClient;
    @Getter
    private final MutableLiveData<List<LogEntry>> mLogEntries = new MutableLiveData<>();
    @Getter
    private final MutableLiveData<List<LogEntry>> mEditLogEntries = new MutableLiveData<>();
    private final List<Disposable> disposables = new ArrayList<>();

    public LogEntryRepository(final Application application) {
        final var db = LocalDatabase.getDatabase(application.getApplicationContext());
        logEntryDao = db.logEntryDao();
        foodDao = db.foodDao();
        final var token = application.getApplicationContext().getSharedPreferences("AUTH", Context.MODE_PRIVATE).getString("TOKEN", null);
        logEntryClient = new LogEntryClient(token);
    }

    // -- Offline first design --

    // Check if local has entries for day
    // If local has entries -> sync local with network, local being leading
    // If not -> Check if network has entries for day
    // If network has entries for day -> sync network with local

    // starting
    // no entries -> fetch -> logentryresponses

    // Model <--> Data/Entity <--> Responses

    public void getLogEntriesForDay(final Date date) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            final var localLogEntries = logEntryDao.getByDate(DateUtil.format(date));
            if (localLogEntries == null || localLogEntries.isEmpty()) {
                fetchInsertAndSetLogEntries(date);
            } else {
                setAndSyncLogEntries(date, localLogEntries);
            }
        });
    }

    public void getLogEntriesForDayAndMeal(final Date date, final Meal meal) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            final var liveDataLogEntries = logEntryDao.getLogEntriesByDateAndMeal(DateUtil.format(date), meal.name());
            final var datas = liveDataLogEntries.getValue();
            final var models = LogEntryMapper.mapDatasToModels(datas);
            mEditLogEntries.postValue(models);
        });
    }

    public void disposeAll() {
        disposables.forEach(disposable -> {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        });
    }

    private void fetchInsertAndSetLogEntries(final Date date) {
        disposables.add(logEntryClient.getLogsForDay(date).observeOn(AndroidSchedulers.mainThread()).subscribe(networkLogEntries -> {
            LocalDatabase.databaseWriteExecutor.execute(() -> {
                final var foodExteranlIds = networkLogEntries.stream().map(logEntryResponse -> logEntryResponse.getFood().getId()).toList();
                final var foodData = foodDao.getByExternalIds(foodExteranlIds);
                final var entities = LogEntryMapper.mapResponsesToEntities(networkLogEntries, foodData);

                logEntryDao.deleteByDate(DateUtil.format(date));
                logEntryDao.insertAll(entities);
                final var datas = logEntryDao.getByDate(DateUtil.format(date));
                final var models = LogEntryMapper.mapDatasToModels(datas);
                mLogEntries.postValue(models);
            });
        }, err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))));
    }

    private void setAndSyncLogEntries(final Date date, final List<LogEntryData> datas) {
        final var models = LogEntryMapper.mapDatasToModels(datas);
        mLogEntries.setValue(models);
        // TODO
//        final var requests = LogEntryMapper.mapDatasToRequests(datas);
//        final var requestsPerMealMap = requests.stream().collect(Collectors.groupingBy(LogEntryRequest::getMeal));
//        requestsPerMealMap.forEach((meal, requestList) -> disposables.add(logEntryClient.postEntries(requestList, date, Meal.valueOf(meal)).observeOn(AndroidSchedulers.mainThread())
//                .subscribe()));
    }
}
