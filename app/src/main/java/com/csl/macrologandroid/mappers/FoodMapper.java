package com.csl.macrologandroid.mappers;

import com.csl.macrologandroid.data.local.entities.FoodEntity;
import com.csl.macrologandroid.data.local.entities.PortionEntity;
import com.csl.macrologandroid.data.models.FoodData;
import com.csl.macrologandroid.dtos.FoodDto;
import com.csl.macrologandroid.models.Food;

import java.util.ArrayList;
import java.util.List;

public class FoodMapper {

    public static FoodEntity mapModelToEntity(final Food model) {
        if (model.getId() != null) {
            return FoodEntity.builder()
                    .id(model.getId())
                    .externalId(model.getId())
                    .name(model.getName())
                    .protein(model.getProtein())
                    .fat(model.getFat())
                    .carbs(model.getCarbs())
                    .build();
        } else {
            return FoodEntity.builder()
                    .externalId(model.getId())
                    .name(model.getName())
                    .protein(model.getProtein())
                    .fat(model.getFat())
                    .carbs(model.getCarbs())
                    .build();
        }
    }

    public static Food mapEntityToModel(final FoodEntity entity, final List<PortionEntity> portionEntities) {
        final var portions = PortionMapper.mapEntitiesToModels(portionEntities);
        return Food.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .name(entity.getName())
                .protein(entity.getProtein())
                .fat(entity.getFat())
                .carbs(entity.getCarbs())
                .portions(portions)
                .build();
    }

    public static List<FoodEntity> mapDtosToEntities(final List<FoodDto> dtos) {
        return dtos.stream().map(FoodMapper::mapDtoToEntity).toList();
    }

    public static FoodEntity mapDtoToEntity(final FoodDto dto) {
        return FoodEntity.builder()
                .externalId(dto.getId()) // Dto id is network/external
                .name(dto.getName())
                .protein(dto.getProtein())
                .fat(dto.getFat())
                .carbs(dto.getCarbs())
                .build();
    }

    public static List<Food> mapDatasToModels(final List<FoodData> datas) {
        if (datas == null) {
            return new ArrayList<>();
        }
        return datas.stream().map(FoodMapper::mapDataToModel).toList();
    }

    public static Food mapDataToModel(final FoodData data) {
        final var portions = PortionMapper.mapEntitiesToModels(data.portionEntities);
        return Food.builder()
                .id(data.foodEntity.getId())
                .externalId(data.foodEntity.getExternalId())
                .name(data.foodEntity.getName())
                .protein(data.foodEntity.getProtein())
                .fat(data.foodEntity.getFat())
                .carbs(data.foodEntity.getCarbs())
                .portions(portions)
                .build();
    }

}
