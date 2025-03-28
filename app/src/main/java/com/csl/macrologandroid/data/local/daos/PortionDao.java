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

    @Query("DELETE FROM portions")
    void deleteAll();

}
