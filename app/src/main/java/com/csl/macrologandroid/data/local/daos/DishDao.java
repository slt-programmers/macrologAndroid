package com.csl.macrologandroid.data.local.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.csl.macrologandroid.data.local.entities.DishEntity;
import com.csl.macrologandroid.data.models.DishData;

import java.util.List;

@Dao
public interface DishDao {

    @Transaction
    @Query("select * from dishes")
    List<DishData> getAllDishes();

    @Insert
    void insertAll(final List<DishEntity> entities);

    @Query("delete from dishes")
    void deleteAll();

}
