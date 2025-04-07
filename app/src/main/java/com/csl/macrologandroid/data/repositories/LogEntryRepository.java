package com.csl.macrologandroid.data.repositories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.local.LocalDatabase;
import com.csl.macrologandroid.data.local.daos.FoodDao;
import com.csl.macrologandroid.data.local.daos.LogEntryDao;
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

    // Local
    private final LogEntryDao logEntryDao;
    private final FoodDao foodDao;
    @Getter
    private final MutableLiveData<List<LogEntry>> mLogEntries = new MutableLiveData<>();
    @Getter
    private final MutableLiveData<List<LogEntry>> mEditLogEntries = new MutableLiveData<>();

    // Network
    private final LogEntryClient logEntryClient;
    private final List<Disposable> disposables = new ArrayList<>();
    @Getter
    private final MutableLiveData<Boolean> mSynced = new MutableLiveData<>(false);

    public LogEntryRepository(final Application application) {
        final var db = LocalDatabase.getDatabase(application.getApplicationContext());
        logEntryDao = db.logEntryDao();
        foodDao = db.foodDao();
        logEntryClient = new LogEntryClient(application.getApplicationContext());
    }

    public void getLogEntriesForDay(final Date date) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            final var localLogEntries = logEntryDao.getByDate(DateUtil.format(date));
            final var models = LogEntryMapper.mapDatasToModels(localLogEntries);
            mLogEntries.postValue(models);
        });
    }

    public void getLogEntriesForDayAndMeal(final Date date, final Meal meal) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            final var datas = logEntryDao.getLogEntriesByDateAndMeal(DateUtil.format(date), meal.name());
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

    public void getNetworkLogEntries(final Date date) {
        final var sevenDaysAgo = new Date(new Date().getTime() - (7 * 1000 * 60 * 60 * 24));
        if (sevenDaysAgo.before(date)) {
            disposables.add(logEntryClient.getLogsForDay(date).observeOn(AndroidSchedulers.mainThread()).subscribe(networkLogEntries -> {
                LocalDatabase.databaseWriteExecutor.execute(() -> {
                    final var foodExteranlIds = networkLogEntries.stream().map(logEntryResponse -> logEntryResponse.getFood().getId()).toList();
                    final var foodData = foodDao.getByExternalIds(foodExteranlIds);
                    final var entities = LogEntryMapper.mapResponsesToEntities(networkLogEntries, foodData);
                    logEntryDao.deleteByDate(DateUtil.format(date));
                    logEntryDao.insertAll(entities);

                    final var time = date.getTime() - (1000 * 60 * 60 * 24);
                    final var previousDay = new Date(time);
                    getNetworkLogEntries(previousDay);
                });
            }, err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))));
        } else {
            mSynced.postValue(true);
        }
    }

}
