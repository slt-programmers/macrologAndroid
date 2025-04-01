package com.csl.macrologandroid.mappers;

import com.csl.macrologandroid.data.local.entities.DishEntity;
import com.csl.macrologandroid.data.models.DishData;
import com.csl.macrologandroid.dtos.DishDto;
import com.csl.macrologandroid.models.Dish;

import java.util.List;

public class DishMapper {

    public static List<Dish> mapDatasToModels(final List<DishData> datas) {
        return datas.stream().map(DishMapper::mapDataToModel).toList();
    }

    public static Dish mapDataToModel(final DishData data) {
        final var ingredients = IngredientMapper.mapDatasToModels(data.ingredientEntities);
        return Dish.builder()
                .id((long) data.dishEntity.getId())
                .externalId(data.dishEntity.getExternalId())
                .name(data.dishEntity.getName())
                .ingredients(ingredients)
                .build();
    }

    public static List<DishEntity> mapDtosToEntities(final List<DishDto> dtos) {
        return dtos.stream().map(DishMapper::maDtoToEntity).toList();
    }

    public static DishEntity maDtoToEntity(final DishDto dto) {
        return DishEntity.builder()
                .externalId(dto.getId())
                .name(dto.getName())
                .build();
    }
}
