package com.csl.macrologandroid.ui.diary;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.csl.macrologandroid.cache.DiaryLogCache;
import com.csl.macrologandroid.cache.UserSettingsCache;
import com.csl.macrologandroid.dtos.LogEntryResponse;
import com.csl.macrologandroid.dtos.MacrosResponse;
import com.csl.macrologandroid.dtos.UserSettingsResponse;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.services.EntryService;
import com.csl.macrologandroid.services.UserService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

import lombok.Getter;

public class DiaryViewModel extends ViewModel {

    private final UserService userService;
    private final EntryService entryService;

    @Getter
    private final MutableLiveData<UserSettingsResponse> mUserSettings;
    @Getter
    private final MutableLiveData<List<LogEntryResponse>> mLogEntries;
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

    public DiaryViewModel() {
        // TODO improve initialization of services
        final var token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2Vycy9Uek1Vb2NNRjRwIiwiZXhwIjoxNzQzMjUwNjg1LCJuYW1lIjoiQ2FybWVuU2Nob2x0ZSIsInVzZXJJZCI6Mn0.9J1kJ6f9e2B-9mpth38PZc6IuPqAs2ylWy-jykmAS5w";
        this.userService = new UserService(token);
        this.entryService = new EntryService(token);
        mUserSettings = new MutableLiveData<>();
        mLogEntries = new MutableLiveData<>();
        initUserSettings();
        getLogEntries(selectedDate);
    }

    public void disposeAll() {
        for (var disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    public void loadNextLogEntries() {
        final var time = selectedDate.getTime() + (1000 * 60 * 60 * 24);
        selectedDate = new Date(time);
        getLogEntries(selectedDate);
    }

    public void loadPreviousLogEntries() {
        final var time = selectedDate.getTime() - (1000 * 60 * 60 * 24);
        selectedDate = new Date(time);
        getLogEntries(selectedDate);
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
        final var logEntries = DiaryLogCache.getInstance().getFromCache(date);
        if (logEntries == null || logEntries.isEmpty()) {
            disposables.add(entryService.getLogsForDay(date)
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

}
