package com.csl.macrologandroid.dtos;

import com.csl.macrologandroid.models.Meal;

import java.io.Serializable;
import java.util.Date;

public class LogEntryResponse  {

    private int id;

    private FoodResponse food;

    private PortionResponse portion;

    private MacrosResponse macrosCalculated;

    private double multiplier;

    private Date day;

    private Meal meal;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public FoodResponse getFood() {
        return food;
    }

    public void setFood(FoodResponse food) {
        this.food = food;
    }

    public PortionResponse getPortion() {
        return portion;
    }

    public void setPortion(PortionResponse portion) {
        this.portion = portion;
    }

    public MacrosResponse getMacrosCalculated() {
        return macrosCalculated;
    }

    public void setMacrosCalculated(MacrosResponse macros) {
        this.macrosCalculated = macros;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public Meal getMeal() {
        return meal;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }

}
