package com.csl.macrologandroid.mappers;

import com.csl.macrologandroid.data.local.entities.ActivityEntity;
import com.csl.macrologandroid.dtos.ActivityResponse;
import com.csl.macrologandroid.models.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityMapper {

    public static List<Activity> mapEntitiesToModels(final List<ActivityEntity> entities) {
        if(entities == null) {
            return new ArrayList<>();
        }
        return entities.stream().map(ActivityMapper::mapEntityToModel).toList();
    }

    public static Activity mapEntityToModel(final ActivityEntity entity) {
        return Activity.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .day(entity.getDay())
                .name(entity.getName())
                .calories(entity.getCalories())
                .syncedId(entity.getSyncedId())
                .syncedWith(entity.getSyncedWith())
                .build();
    }

    public static List<ActivityEntity> mapResponsesToEntities(final List<ActivityResponse> responses) {
        if (responses == null) {
            return new ArrayList<>();
        }
        return responses.stream().map(ActivityMapper::mapResponseToEntity).toList();
    }

    public static ActivityEntity mapResponseToEntity(final ActivityResponse response) {
        return ActivityEntity.builder()
                .externalId(response.getId())
                .name(response.getName())
                .syncedWith(response.getSyncedWith())
                .syncedId(response.getSyncedId())
                .calories(response.getCalories())
                .build();
    }
}
