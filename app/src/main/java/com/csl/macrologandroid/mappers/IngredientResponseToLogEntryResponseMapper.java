package com.csl.macrologandroid.mappers;

import com.csl.macrologandroid.dtos.IngredientResponse;
import com.csl.macrologandroid.dtos.LogEntryResponse;
import com.csl.macrologandroid.models.Meal;

import java.util.Date;

public class IngredientResponseToLogEntryResponseMapper {

    public static LogEntryResponse map(final IngredientResponse ingredient, final Date date, final Meal meal) {
        final var logEntry = new LogEntryResponse();
        logEntry.setFood(ingredient.getFood());
        logEntry.setPortion(ingredient.getPortion());
        logEntry.setMultiplier(ingredient.getMultiplier());
        logEntry.setDay(date);
        logEntry.setMeal(meal);
        return logEntry;
    }
}
