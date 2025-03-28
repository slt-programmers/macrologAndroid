package com.csl.macrologandroid.data.repositories;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.local.LocalDatabase;
import com.csl.macrologandroid.data.local.daos.LogEntryDao;
import com.csl.macrologandroid.data.models.LogEntryData;
import com.csl.macrologandroid.dtos.LogEntryRequest;
import com.csl.macrologandroid.mappers.LogEntryMapper;
import com.csl.macrologandroid.models.LogEntry;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.services.LogEntryClient;
import com.csl.macrologandroid.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

public class LogEntryRepository {

    private final LogEntryDao logEntryDao;
    private final LogEntryClient logEntryClient;
    private final MutableLiveData<List<LogEntry>> mLogEntries = new MutableLiveData<>();
    private final List<Disposable> disposables = new ArrayList<>();

    public LogEntryRepository(final Application application) {
        final var db = LocalDatabase.getDatabase(application.getApplicationContext());
        logEntryDao = db.logEntryDao();
        final var token = application.getApplicationContext().getSharedPreferences("AUTH", Context.MODE_PRIVATE).getString("TOKEN", null);
        logEntryClient = new LogEntryClient(token);
    }

    public MutableLiveData<List<LogEntry>> getMLogEntries() {
        return mLogEntries;
    }

    // -- Offline first design --

    // Check if local has entries for day
    // If local has entries -> sync local with network, local being leading
    // If not -> Check if network has entries for day
    // If network has entries for day -> sync network with local

    // Model <--> Data/Entity <--> Responses

    public void getLogEntriesForDay(final Date date) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            final var liveDataLogEntries = logEntryDao.getLogEntriesByDate(DateUtil.format(date));
            final var localLogEntries = liveDataLogEntries.getValue();
            if (localLogEntries == null || localLogEntries.isEmpty()) {
                fetchInsertAndSetLogEntries(date);
            } else {
                setAndSyncLogEntries(date, localLogEntries);
            }
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
        disposables.add(logEntryClient.getLogsForDay(date).observeOn(AndroidSchedulers.mainThread())
                .subscribe(networkLogEntries -> {
                    final var entities = LogEntryMapper.mapResponsesToEntities(networkLogEntries);
                    LocalDatabase.databaseWriteExecutor.execute(() -> logEntryDao.insertAll(entities));
//                    LocalDatabase.databaseWriteExecutor.execute(() -> {
                    final var datas = logEntryDao.getLogEntriesByDate(DateUtil.format(date));
                    final var models = LogEntryMapper.mapDatasToModels(datas.getValue());
                    mLogEntries.setValue(models);

//                    });


                }, err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))));
    }

    private void setAndSyncLogEntries(final Date date, final List<LogEntryData> datas) {
        final var models = LogEntryMapper.mapDatasToModels(datas);
        mLogEntries.setValue(models);

        final var requests = LogEntryMapper.mapDatasToRequests(datas);
        final var requestsPerMealMap = requests.stream().collect(Collectors.groupingBy(LogEntryRequest::getMeal));
        requestsPerMealMap.forEach((meal, requestList) -> disposables.add(logEntryClient.postEntries(requestList, date, Meal.valueOf(meal)).observeOn(AndroidSchedulers.mainThread())
                .subscribe()));
    }
}
