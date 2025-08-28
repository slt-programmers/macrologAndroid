package com.csl.macrologandroid.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class Activity {

    private Long id;
    private Long externalId;
    private String name;
    private String day;
    private Integer calories;
    private String syncedWith;
    private Long syncedId;

}
