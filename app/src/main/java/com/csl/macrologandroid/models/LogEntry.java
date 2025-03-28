package com.csl.macrologandroid.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Builder
public class LogEntry {

    private final Long id;
    private final Long externalId;
    private final Food food;
    @Setter
    private Portion portion;
    @Setter
    private Double multiplier;
    private final String day;
    private final String meal;

}
