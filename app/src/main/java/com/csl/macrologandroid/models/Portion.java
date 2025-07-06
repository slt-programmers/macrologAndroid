package com.csl.macrologandroid.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Portion implements Serializable {

    private final Long id;
    private final Long externalId;
    private final Double grams;
    private final String description;

}
