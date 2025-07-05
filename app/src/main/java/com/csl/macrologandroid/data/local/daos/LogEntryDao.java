package com.csl.macrologandroid.data.local.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
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
    List<LogEntryData> getLogEntriesByDateAndMeal(final String date, final String meal);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(final List<LogEntryEntity> logEntryEntities);

    @Query("delete from logentries where id = :id")
    void delete(final Long id);

    @Query("delete from logentries where day = :date")
    void deleteByDate(final String date);
}