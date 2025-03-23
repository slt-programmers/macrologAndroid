package com.csl.macrologandroid.data.local.daos;

import androidx.room.Dao;
import androidx.room.Query;

import com.csl.macrologandroid.data.local.entities.LogEntryEntity;

import java.util.List;

@Dao
public interface LogEntryDao {

    @Query("SELECT * FROM logentryentity WHERE day = :date")
    List<LogEntryEntity> getByDate(final String date);

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
