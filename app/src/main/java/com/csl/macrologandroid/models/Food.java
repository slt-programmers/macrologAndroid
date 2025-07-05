package com.csl.macrologandroid.models;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class Food implements Serializable {

    private final Long id;
    private final Long externalId;
    private final String name;
    private final Double protein;
    private final Double fat;
    private final Double carbs;
    private final List<Portion> portions;
}
