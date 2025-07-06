package com.csl.macrologandroid.data.local.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.csl.macrologandroid.data.local.entities.UserSettingsEntity;

@Dao
public interface UserSettingsDao {

    @Transaction
    @Query("select * from usersettings limit 1")
    UserSettingsEntity get();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final UserSettingsEntity entity);

}
