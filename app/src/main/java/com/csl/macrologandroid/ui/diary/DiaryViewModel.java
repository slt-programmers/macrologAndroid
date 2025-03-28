package com.csl.macrologandroid.ui.diary;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.cache.DiaryLogCache;
import com.csl.macrologandroid.cache.UserSettingsCache;
import com.csl.macrologandroid.data.repositories.LogEntryRepository;
import com.csl.macrologandroid.dtos.ActivityResponse;
import com.csl.macrologandroid.dtos.LogEntryResponse;
import com.csl.macrologandroid.dtos.MacrosResponse;
import com.csl.macrologandroid.dtos.UserSettingsResponse;
import com.csl.macrologandroid.models.LogEntry;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.services.ActivityService;
import com.csl.macrologandroid.services.LogEntryClient;
import com.csl.macrologandroid.services.UserService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

import lombok.Getter;

public class DiaryViewModel extends AndroidViewModel {

    private final UserService userService;
    private final LogEntryRepository logEntryRepository;
    private final LogEntryClient logEntryClient;
    private final ActivityService activityService;

    @Getter
    private final MutableLiveData<UserSettingsResponse> mUserSettings;
    @Getter
    private final MutableLiveData<List<LogEntryResponse>> mLogEntries;
    @Getter
    private final MutableLiveData<List<LogEntry>> mLocalLogEntries;
    @Getter
    private final MutableLiveData<List<ActivityResponse>> mActivities;
    @Getter
    private final List<LogEntryResponse> breakfastEntries = new ArrayList<>();
    @Getter
    private final List<LogEntryResponse> lunchEntries = new ArrayList<>();
    @Getter
    private final List<LogEntryResponse> dinnerEntries = new ArrayList<>();
    @Getter
    private final List<LogEntryResponse> snacksEntries = new ArrayList<>();
    @Getter
    private double totalProtein;
    @Getter
    private double totalFat;
    @Getter
    private double totalCarbs;
    @Getter
    private int totalCalories;

    @Getter
    // TODO refactor to localdate
    private Date selectedDate = new Date();

    private final List<Disposable> disposables = new ArrayList<>();

    public DiaryViewModel(final Application application) {
        super(application);
        final var token = application.getApplicationContext().getSharedPreferences("AUTH", Context.MODE_PRIVATE).getString("TOKEN", null);
        this.userService = new UserService(token);
        this.logEntryRepository = new LogEntryRepository(application);
        this.logEntryClient = new LogEntryClient(token);
        this.activityService = new ActivityService(token);
        mUserSettings = new MutableLiveData<>();
        mLogEntries = new MutableLiveData<>();
        mLocalLogEntries = logEntryRepository.getMLogEntries();
        mActivities = new MutableLiveData<>();
        initUserSettings();
        getLogEntries(selectedDate);
        getActivities(selectedDate);
    }

    public void disposeAll() {
        for (var disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    public void loadNextDate() {
        final var time = selectedDate.getTime() + (1000 * 60 * 60 * 24);
        selectedDate = new Date(time);
        getLogEntries(selectedDate);
        getActivities(selectedDate);
    }

    public void loadPreviousDate() {
        final var time = selectedDate.getTime() - (1000 * 60 * 60 * 24);
        selectedDate = new Date(time);
        getLogEntries(selectedDate);
        getActivities(selectedDate);
    }

    public void syncActivities() {
        getActivities(selectedDate);
    }

    private void initUserSettings() {
        final var settings = UserSettingsCache.getInstance().getCache();
        if (settings == null) {
            disposables.add(userService.getUserSettings()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(res -> {
                        UserSettingsCache.getInstance().updateCache(res);
                        mUserSettings.setValue(res);
                    }, err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))));
        } else {
            mUserSettings.setValue(settings);
        }
    }

    private void getLogEntries(final Date date) {
        getLocalLogEntries(date);
        final var logEntries = DiaryLogCache.getInstance().getFromCache(date);
        if (logEntries == null || logEntries.isEmpty()) {
            disposables.add(logEntryClient.getLogsForDay(date)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(res -> {
                        DiaryLogCache.getInstance().addToCache(date, res);
                        sortEntriesAndSetTotals(res);
                        mLogEntries.setValue(res);
                    }, err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))));
        } else {
            sortEntriesAndSetTotals(logEntries);
            mLogEntries.setValue(logEntries);
        }
    }

    private void getLocalLogEntries(final Date date) {
        logEntryRepository.getLogEntriesForDay(date);
    }

    private void sortEntriesAndSetTotals(final List<LogEntryResponse> logEntries) {
        resetAllState();
        for (LogEntryResponse logEntry : logEntries) {
            final var macros = logEntry.getMacrosCalculated();
            addMacros(macros);

            if (logEntry.getMeal() == Meal.BREAKFAST) {
                breakfastEntries.add(logEntry);
            } else if (logEntry.getMeal() == Meal.LUNCH) {
                lunchEntries.add(logEntry);
            } else if (logEntry.getMeal() == Meal.DINNER) {
                dinnerEntries.add(logEntry);
            } else {
                snacksEntries.add(logEntry);
            }
        }
        totalCalories = (int) ((totalProtein * 4) + (totalFat * 9) + (totalCarbs * 4));
    }

    private void addMacros(final MacrosResponse macros) {
        totalProtein += macros.getProtein();
        totalFat += macros.getFat();
        totalCarbs += macros.getCarbs();
    }

    private void resetAllState() {
        breakfastEntries.clear();
        lunchEntries.clear();
        dinnerEntries.clear();
        snacksEntries.clear();

        totalProtein = 0;
        totalFat = 0;
        totalCarbs = 0;
    }

    private void getActivities(final Date date) {
        disposables.add(activityService.getActivitiesForDay(date)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mActivities::setValue, err -> Log.e(this.getClass().getName(), Objects.requireNonNull(err.getMessage()))));
    }
}
