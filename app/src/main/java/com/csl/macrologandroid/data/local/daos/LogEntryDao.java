package com.csl.macrologandroid.data.local.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.csl.macrologandroid.data.local.entities.LogEntryEntity;
import com.csl.macrologandroid.data.models.LogEntryData;

import java.util.List;

@Dao
public interface LogEntryDao {

    @Transaction
    @Query("select * from logentries where day = :date")
    List<LogEntryData> getByDate(final String date);

    @Transaction
    @Query("select * from logentries where day = :date and meal = :meal")
    LiveData<List<LogEntryData>> getLogEntriesByDateAndMeal(final String date, final String meal);

    @Insert
    void insertAll(final List<LogEntryEntity> logEntryEntities);

    @Delete
    void delete(final LogEntryEntity logEntryEntity);

    @Query("delete from logentries where day = :date")
    void deleteByDate(final String date);
}