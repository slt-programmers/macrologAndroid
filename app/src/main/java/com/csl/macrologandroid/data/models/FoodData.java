package com.csl.macrologandroid.data.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.csl.macrologandroid.data.local.entities.FoodEntity;
import com.csl.macrologandroid.data.local.entities.PortionEntity;

import java.util.List;

public class FoodData {

    @Embedded
    public FoodEntity foodEntity;
    @Relation(parentColumn = "id", entityColumn = "food_id")
    public List<PortionEntity> portionEntities;

}
