package com.csl.macrologandroid.data.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.csl.macrologandroid.data.local.entities.FoodEntity;
import com.csl.macrologandroid.data.local.entities.IngredientEntity;
import com.csl.macrologandroid.data.local.entities.PortionEntity;

public class IngredientData {

    @Embedded
    public IngredientEntity ingredientEntity;
    @Relation(parentColumn = "food_id", entityColumn = "id", entity = FoodEntity.class)
    public FoodData foodData;
    @Relation(parentColumn = "portion_id", entityColumn = "id")
    public PortionEntity portionEntity;

}
