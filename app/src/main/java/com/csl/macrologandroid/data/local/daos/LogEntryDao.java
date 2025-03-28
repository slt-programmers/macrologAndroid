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

    @Query("SELECT * FROM logentries WHERE day = :date")
    List<LogEntryEntity> getByDate(final String date);

    @Transaction
    @Query("SELECT * FROM logentries WHERE day = :date")
    LiveData<List<LogEntryData>> getLogEntriesByDate(final String date);

    @Insert
    void insertAll(final List<LogEntryEntity> logEntryEntities);

    @Delete
    void delete(final LogEntryEntity logEntryEntity);
}

//public interface UserDao {
//    @Query("SELECT * FROM user")
//    List<User> getAll();
//
//    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
//    List<User> loadAllByIds(int[] userIds);
//
//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    User findByName(String first, String last);
//
//    @Insert
//    void insertAll(User... users);
//
//    @Delete
//    void delete(User user);
//}
