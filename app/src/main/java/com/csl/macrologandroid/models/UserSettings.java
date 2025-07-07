package com.csl.macrologandroid.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class UserSettings {

    private String name;
    private String birthday;
    private Gender gender;
    private Integer height;
    private Double currentWeight;
    private Double activity;
    private Integer goalProtein;
    private Integer goalFat;
    private Integer goalCarbs;

}
