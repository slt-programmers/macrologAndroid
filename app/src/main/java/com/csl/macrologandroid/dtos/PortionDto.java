package com.csl.macrologandroid.dtos;

import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PortionDto implements Serializable {

    private final Long id;

    private final double grams;

    private final String description;

    public PortionDto(Long id, double grams, String description) {
        this.id = id;
        this.grams = grams;
        this.description = description;
    }

}
