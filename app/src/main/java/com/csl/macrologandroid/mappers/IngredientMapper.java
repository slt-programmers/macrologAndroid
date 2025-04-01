package com.csl.macrologandroid.mappers;

import com.csl.macrologandroid.data.local.entities.IngredientEntity;
import com.csl.macrologandroid.dtos.IngredientDto;
import com.csl.macrologandroid.models.Ingredient;

import java.util.List;

public class IngredientMapper {

    public static List<Ingredient> mapDatasToModels(final List<IngredientEntity> entities) {
        return entities.stream().map(IngredientMapper::mapDataToModel).toList();
    }

    public static Ingredient mapDataToModel(final IngredientEntity entity) {
        return Ingredient.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .foodId(entity.getFoodId())
                .portionId(entity.getPortionId())
                .multiplier(entity.getMultiplier())
                .build();
    }

    public static List<IngredientEntity> mapDtosToEntities(final List<IngredientDto> dtos, final Long dishId) {
        return dtos.stream().map(dto -> IngredientMapper.mapDtoToEntity(dto, dishId)).toList();
    }

    public static IngredientEntity mapDtoToEntity(final IngredientDto dto, final Long dishId) {
        final var portionId = dto.getPortion() != null ? dto.getPortion().getId() : null;
        return IngredientEntity.builder()
                // No external id, maybe a problem
                .foodId(dto.getFood().getId())
                .dishId(dishId)
                .portionId(portionId)
                .multiplier(dto.getMultiplier())
                .build();
    }
}
