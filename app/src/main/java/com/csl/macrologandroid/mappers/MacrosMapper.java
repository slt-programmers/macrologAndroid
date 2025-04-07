package com.csl.macrologandroid.mappers;

import com.csl.macrologandroid.models.Food;
import com.csl.macrologandroid.models.Macros;
import com.csl.macrologandroid.models.Portion;

public class MacrosMapper {

    public static Macros mapMacros(final Food food, final Portion portion, final Double multiplier) {
        if (portion == null) {
            final var protein = food.getProtein() * multiplier;
            final var fat = food.getFat() * multiplier;
            final var carbs = food.getCarbs() * multiplier;
            return Macros.builder()
                    .protein(protein)
                    .fat(fat)
                    .carbs(carbs)
                    .calories((protein * 4) + (fat * 9) + (carbs * 4))
                    .build();
        } else {
            final var protein = food.getProtein() * multiplier * (portion.getGrams() / 100);
            final var fat = food.getFat() * multiplier * (portion.getGrams() / 100);
            final var carbs = food.getCarbs() * multiplier * (portion.getGrams() / 100);
            return Macros.builder()
                    .protein(protein)
                    .fat(fat)
                    .carbs(carbs)
                    .calories((protein * 4) + (fat * 9) + (carbs * 4))
                    .build();
        }
    }
}
