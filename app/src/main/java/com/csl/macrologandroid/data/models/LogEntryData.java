package com.csl.macrologandroid.data.models;


import androidx.room.Embedded;
import androidx.room.Relation;

import com.csl.macrologandroid.data.local.entities.FoodEntity;
import com.csl.macrologandroid.data.local.entities.LogEntryEntity;
import com.csl.macrologandroid.data.local.entities.PortionEntity;

import java.util.List;

public class LogEntryData {

    @Embedded
    public LogEntryEntity logEntryEntity;
    @Relation(parentColumn = "food_id", entityColumn = "id")
    public FoodEntity foodForEntity;
    @Relation(parentColumn = "portion_id", entityColumn = "id")
    public PortionEntity portionForEntity;
    @Relation(parentColumn = "food_id", entityColumn = "food_id")
    public List<PortionEntity> portionsForFood;

}
