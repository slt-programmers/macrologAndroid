package com.csl.macrologandroid.data.repositories;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.csl.macrologandroid.data.local.LocalDatabase;
import com.csl.macrologandroid.data.local.daos.LogEntryDao;
import com.csl.macrologandroid.data.local.entities.LogEntryEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogEntryRepository {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

    private LogEntryDao logEntryDao;

    private MutableLiveData<List<LogEntryEntity>> mLogEntries = new MutableLiveData<>();

    public LogEntryRepository(final Application application) {
        final var db = LocalDatabase.getDatabase(application.getApplicationContext());
        logEntryDao = db.logEntryDao();
    }

    public MutableLiveData<List<LogEntryEntity>> getMLogEntries() {
        return mLogEntries;
    }

    public void getLogEntriesForDay(final Date date) {
        mLogEntries.setValue(logEntryDao.getByDate(simpleDateFormat.format(date)));
    }

//    void insert(Word word) {
//        WordRoomDatabase.databaseWriteExecutor.execute(() -> {
//            mWordDao.insert(word);
//        });
//    }
}
