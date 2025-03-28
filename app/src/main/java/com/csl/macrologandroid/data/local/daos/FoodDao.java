package com.csl.macrologandroid.data.local.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Relation;
import androidx.room.Transaction;

import com.csl.macrologandroid.data.local.entities.FoodEntity;
import com.csl.macrologandroid.data.local.entities.PortionEntity;
import com.csl.macrologandroid.data.models.FoodData;
import com.csl.macrologandroid.data.models.LogEntryData;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface FoodDao {

    @Transaction
    @Query("SELECT * FROM food")
    List<FoodData> getAllFood();

    @Insert
    void insertAll(List<FoodEntity> entities);

    @Insert
    long insert(final FoodEntity entity);

    @Query("DELETE FROM food")
    void deleteAll();

}
