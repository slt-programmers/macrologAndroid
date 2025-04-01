package com.csl.macrologandroid.data.local.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.csl.macrologandroid.data.local.entities.IngredientEntity;

import java.util.List;

@Dao
public interface IngredientDao {

    @Query("delete from ingredients")
    void deleteAll();

    @Insert
    void insertAll(final List<IngredientEntity> entities);
}
