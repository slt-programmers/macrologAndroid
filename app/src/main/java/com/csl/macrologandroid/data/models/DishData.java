package com.csl.macrologandroid.data.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.csl.macrologandroid.data.local.entities.DishEntity;
import com.csl.macrologandroid.data.local.entities.IngredientEntity;

import java.util.List;

public class DishData {

    @Embedded
    public DishEntity dishEntity;
    @Relation(parentColumn = "id", entityColumn = "dish_id", entity = IngredientEntity.class)
    public List<IngredientData> ingredientEntities;

}
