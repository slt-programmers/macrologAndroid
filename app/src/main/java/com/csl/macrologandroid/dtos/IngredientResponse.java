package com.csl.macrologandroid.dtos;

import java.io.Serializable;

public class IngredientResponse implements Serializable {

    private Double multiplier;
    private FoodDto food;
    private PortionDto portion;

    public IngredientResponse(Double multiplier, FoodDto food, PortionDto portion) {
        this.multiplier = multiplier;
        this.food = food;
        this.portion = portion;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
    }

    public FoodDto getFood() {
        return food;
    }

    public void setFood(FoodDto food) {
        this.food = food;
    }

    public PortionDto getPortion() {
        return portion;
    }

    public void setPortion(PortionDto portion) {
        this.portion = portion;
    }
}
