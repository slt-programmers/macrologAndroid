package com.csl.macrologandroid.mappers;

import com.csl.macrologandroid.data.local.entities.IngredientEntity;
import com.csl.macrologandroid.data.models.IngredientData;
import com.csl.macrologandroid.dtos.IngredientDto;
import com.csl.macrologandroid.models.Ingredient;

import java.util.List;

public class IngredientMapper {

    public static List<Ingredient> mapDatasToModels(final List<IngredientData> datas) {
        return datas.stream().map(IngredientMapper::mapDataToModel).toList();
    }

    public static Ingredient mapDataToModel(final IngredientData data) {
        return Ingredient.builder()
                .id(data.ingredientEntity.getId())
                .externalId(data.ingredientEntity.getExternalId())
                .food(FoodMapper.mapEntityToModel(data.foodEntity, List.of()))
                .portion(PortionMapper.mapEntityToModel(data.portionEntity))
                .multiplier(data.ingredientEntity.getMultiplier())
                .build();
    }

    public static IngredientEntity mapDtoToEntity(final IngredientDto dto, final Long dishId,
                                                  final Long foodId, final Long portionId) {
        return IngredientEntity.builder()
                .externalId(dto.getId())
                .dishId(dishId)
                .foodId(foodId)
                .portionId(portionId)
                .multiplier(dto.getMultiplier())
                .build();
    }
}
