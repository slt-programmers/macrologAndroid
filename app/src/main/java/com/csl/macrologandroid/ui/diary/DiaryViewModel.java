package com.csl.macrologandroid.ui.diary;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.repositories.ActivityRepository;
import com.csl.macrologandroid.data.repositories.LogEntryRepository;
import com.csl.macrologandroid.data.repositories.UserSettingsRepository;
import com.csl.macrologandroid.models.Activity;
import com.csl.macrologandroid.models.LogEntry;
import com.csl.macrologandroid.models.Macros;
import com.csl.macrologandroid.models.Meal;
import com.csl.macrologandroid.models.UserSettings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;

public class DiaryViewModel extends AndroidViewModel {

    private final UserSettingsRepository userSettingsRepository;
    private final LogEntryRepository logEntryRepository;
    private final ActivityRepository activityRepository;

    @Getter
    private final MutableLiveData<UserSettings> mUserSettings;
    @Getter
    private final MutableLiveData<List<LogEntry>> mLogEntries;
    @Getter
    private final MutableLiveData<List<Activity>> mActivities;
    @Getter
    private final List<LogEntry> breakfastEntries = new ArrayList<>();
    @Getter
    private final List<LogEntry> lunchEntries = new ArrayList<>();
    @Getter
    private final List<LogEntry> dinnerEntries = new ArrayList<>();
    @Getter
    private final List<LogEntry> snacksEntries = new ArrayList<>();
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

    public DiaryViewModel(final Application app) {
        super(app);
        this.userSettingsRepository = new UserSettingsRepository(app);
        this.logEntryRepository = new LogEntryRepository(app);
        this.activityRepository = new ActivityRepository(getApplication());
        mUserSettings = userSettingsRepository.getMUserSettings();
        mLogEntries = new MutableLiveData<>();
        mActivities = new MutableLiveData<>();
    }

    public void loadCurrentDate() {
        userSettingsRepository.getUserSettings();
        getLogEntries(selectedDate);
        getActivities(selectedDate);
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

    private void getLogEntries(final Date date) {
        logEntryRepository.getMLogEntriesForDay().observeForever(logEntries -> {
            sortEntriesAndSetTotals(logEntries);
            mLogEntries.setValue(logEntries);
        });
        logEntryRepository.getLogEntriesForDay(date);
    }

    private void sortEntriesAndSetTotals(final List<LogEntry> logEntries) {
        resetAllState();
        for (var logEntry : logEntries) {
            final var macros = logEntry.getMacros();
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

    private void addMacros(final Macros macros) {
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
        activityRepository.getMActivitiesForDay().observeForever(mActivities::setValue);
        activityRepository.getActivitiesForDay(date);
    }
}
