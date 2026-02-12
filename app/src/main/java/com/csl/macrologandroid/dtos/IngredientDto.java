package com.csl.macrologandroid.dtos;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;

@Data
public class IngredientDto implements Serializable {

    private Long id; // externalId
    private Double multiplier;
    private FoodDto food;
    private PortionDto portion;

    public IngredientDto(Long id, Double multiplier, FoodDto food, PortionDto portion) {
        this.id = id;
        this.multiplier = multiplier;
        this.food = food;
        this.portion = portion;
    }

}
