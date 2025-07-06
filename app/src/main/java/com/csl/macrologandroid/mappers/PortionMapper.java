package com.csl.macrologandroid.mappers;

import com.csl.macrologandroid.data.local.entities.PortionEntity;
import com.csl.macrologandroid.dtos.PortionDto;
import com.csl.macrologandroid.models.Portion;

import java.util.List;

public class PortionMapper {

    public static List<PortionEntity> mapModelsToEntities(final List<Portion> models, final long foodId) {
        return models.stream().map(model -> mapModelToEntity(model, foodId)).toList();
    }

    public static PortionEntity mapModelToEntity(final Portion model, final long foodId) {
        if (model.getId() != null) {
            return PortionEntity.builder()
                    .id(model.getId())
                    .externalId(model.getExternalId())
                    .description(model.getDescription())
                    .grams(model.getGrams())
                    .foodId(foodId)
                    .build();
        } else {
            return PortionEntity.builder()
                    .externalId(model.getExternalId())
                    .description(model.getDescription())
                    .grams(model.getGrams())
                    .foodId(foodId)
                    .build();
        }
    }

    public static List<Portion> mapEntitiesToModels(final List<PortionEntity> entities) {
        return entities.stream().map(PortionMapper::mapEntityToModel).toList();
    }

    public static Portion mapEntityToModel(final PortionEntity entity) {
        if (entity == null) return null;
        return Portion.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .grams(entity.getGrams())
                .description(entity.getDescription())
                .build();
    }

    public static List<PortionDto> mapEntitiesToDtos(final List<PortionEntity> entities) {
        return entities.stream().map(PortionMapper::mapEntityToDto).toList();
    }

    public static PortionDto mapEntityToDto(final PortionEntity entity) {
        return PortionDto.builder()
                .id(entity.getExternalId())
                .description(entity.getDescription())
                .grams(entity.getGrams())
                .build();
    }

    public static List<PortionEntity> mapDtosToEntities(final List<PortionDto> dtos, final Long foodId) {
        return dtos.stream().map(dto -> PortionMapper.mapDtoToEntity(dto, foodId)).toList();
    }

    public static PortionEntity mapDtoToEntity(final PortionDto dto, final Long foodId) {
        return PortionEntity.builder()
                .externalId(dto.getId())
                .foodId(foodId)
                .grams(dto.getGrams())
                .description(dto.getDescription())
                .build();
    }
}
