package com.csl.macrologandroid.data.local.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.csl.macrologandroid.data.local.entities.PortionEntity;

import java.util.List;

@Dao
public interface PortionDao {

    @Insert
    void insertAll(final List<PortionEntity> entities);

    @Query("delete from portions where id = :id")
    void delete(final Long id);

    @Query("delete from portions where food_id = :foodId")
    void deleteForFood(final Long foodId);
}
