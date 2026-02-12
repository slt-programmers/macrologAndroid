package com.csl.macrologandroid.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Ingredient {

    private final Long id;
    private final Long externalId;
    private final Food food;
    private final Portion portion;
    private final Double multiplier;

}
