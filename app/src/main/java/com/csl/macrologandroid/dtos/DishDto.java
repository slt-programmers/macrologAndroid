package com.csl.macrologandroid.dtos;

import java.io.Serializable;
import java.util.List;

public class DishDto implements Serializable {

    private Long id;
    private final String name;
    private final List<IngredientDto> ingredients;

    public DishDto(Long id, String name, List<IngredientDto> ingredients) {
        this.id = id;
        this.name = name;
        this.ingredients = ingredients;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public List<IngredientDto> getIngredients() {
        return ingredients;
    }

}
