package com.csl.macrologandroid.data.repositories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.local.LocalDatabase;
import com.csl.macrologandroid.data.local.daos.ActivityDao;
import com.csl.macrologandroid.data.network.ActivityClient;
import com.csl.macrologandroid.mappers.ActivityMapper;
import com.csl.macrologandroid.models.Activity;
import com.csl.macrologandroid.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.Getter;

public class ActivityRepository {

    private final ActivityDao activityDao;
    private final ActivityClient activityClient;
    @Getter
    private final MutableLiveData<Boolean> mSynced = new MutableLiveData<>(false);
    @Getter
    private final MutableLiveData<List<Activity>> mActivitiesForDay = new MutableLiveData<>();

    public ActivityRepository(final Application application) {
        final var db = LocalDatabase.getDatabase(application.getApplicationContext());
        activityDao = db.activityDao();
        activityClient = new ActivityClient(application.getApplicationContext());
    }

    public void getActivitiesForDay(final Date date) {
        LocalDatabase.databaseWriteExecutor.execute(() -> {
            final var localActivities = activityDao.getByDate(DateUtil.format(date));
            final var models = ActivityMapper.mapEntitiesToModels(localActivities);
            mActivitiesForDay.postValue(models);
        });
    }

    public List<Disposable> getNetworkActivities(final Date date) {
        final var fourteenDaysAgo = new Date(new Date().getTime() - (14 * 1000 * 60 * 60 * 24));
        final List<Disposable> disposables = new ArrayList<>();
        if (fourteenDaysAgo.before(date)) {
            disposables.add(activityClient.getActivitiesForDay(date).observeOn(AndroidSchedulers.mainThread()).subscribe(networkActivities -> LocalDatabase.databaseWriteExecutor.execute(() -> {
                final var entities = ActivityMapper.mapResponsesToEntities(networkActivities);
                activityDao.insertAll(entities);

                final var time = date.getTime() - (1000 * 60 * 60 * 24);
                final var previousDay = new Date(time);
                getNetworkActivities(previousDay);
            }), err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))));
        } else {
            mSynced.postValue(true);
        }
        return disposables;
    }
}
