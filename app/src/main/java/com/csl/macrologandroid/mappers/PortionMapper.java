package com.csl.macrologandroid.mappers;

import com.csl.macrologandroid.data.local.entities.PortionEntity;
import com.csl.macrologandroid.dtos.PortionDto;
import com.csl.macrologandroid.models.Portion;

import java.util.List;

public class PortionMapper {

    public static List<Portion> mapEntitiesToModels(final List<PortionEntity> entities) {
        return entities.stream().map(PortionMapper::mapEntityToModel).toList();
    }

    public static Portion mapEntityToModel(final PortionEntity entity) {
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
