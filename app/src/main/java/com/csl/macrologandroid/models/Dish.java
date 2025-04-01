package com.csl.macrologandroid.models;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Dish {

    private final Long id;
    private final Long externalId;
    private final String name;
    private final List<Ingredient> ingredients;

}
