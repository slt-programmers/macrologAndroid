package com.csl.macrologandroid.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Portion {

    private final Long id;
    private final Long externalId;
    private final Double grams;
    private final String description;

}
