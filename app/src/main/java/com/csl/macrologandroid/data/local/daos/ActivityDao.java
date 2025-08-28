package com.csl.macrologandroid.data.local.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.csl.macrologandroid.data.local.entities.ActivityEntity;

import java.util.List;

@Dao
public interface ActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(final List<ActivityEntity> activityEntities);

    @Transaction
    @Query("select * from activities where day = :day")
    List<ActivityEntity> getByDate(final String day);

}
