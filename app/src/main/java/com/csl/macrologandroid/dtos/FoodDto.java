package com.csl.macrologandroid.dtos;

import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class FoodDto implements Serializable {

    // Both Request and Response

    @Setter
    private Long id;
    private final String name;
    private final double protein;
    private final double fat;
    private final double carbs;
    private final List<PortionDto> portions;

    public FoodDto(Long id, String name, double protein, double fat, double carbs, List<PortionDto> portions) {
        this.id = id;
        this.name = name;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.portions = portions;
    }

}
