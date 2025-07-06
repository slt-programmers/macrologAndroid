package com.csl.macrologandroid.data.local.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.csl.macrologandroid.data.local.entities.FoodEntity;
import com.csl.macrologandroid.data.models.FoodData;

import java.util.List;

@Dao
public interface FoodDao {

    @Transaction
    @Query("select * from food")
    List<FoodData> getAllFood();

    @Transaction
    @Query("select * from food where external_id in (:externalIds)")
    List<FoodData> getByExternalIds(final List<Long> externalIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(final List<FoodEntity> entities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(final FoodEntity entity);

    @Query("delete from food where id = :id")
    void delete(final Long id);
}
