package com.csl.macrologandroid.data.local;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.csl.macrologandroid.data.local.daos.LogEntryDao;
import com.csl.macrologandroid.data.local.entities.LogEntryEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {LogEntryEntity.class}, version = 1)
public abstract class LocalDatabase extends RoomDatabase {
    public abstract LogEntryDao logEntryDao();

    private static volatile LocalDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static LocalDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LocalDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    LocalDatabase.class, "macrolog_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
