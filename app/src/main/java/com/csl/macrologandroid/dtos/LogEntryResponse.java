package com.csl.macrologandroid.dtos;

import com.csl.macrologandroid.models.Meal;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LogEntryResponse  {

    private int id;

    private FoodDto food;

    private PortionDto portion;

    private MacrosResponse macrosCalculated;

    private double multiplier;

    private Date day;

    private Meal meal;

}
