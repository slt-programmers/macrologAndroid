package com.csl.macrologandroid.dtos;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MacrosResponse implements Serializable {

    private double protein;

    private double fat;

    private double carbs;

    private double calories;

    public MacrosResponse(double protein, double fat, double carbs, double calories) {
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.calories = calories;
    }

}
