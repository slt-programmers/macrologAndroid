package com.csl.macrologandroid.mappers;

import com.csl.macrologandroid.data.local.entities.UserSettingsEntity;
import com.csl.macrologandroid.dtos.UserSettingsResponse;
import com.csl.macrologandroid.models.UserSettings;
import com.csl.macrologandroid.util.DateUtil;

public class UserSettingsMapper {


    public static UserSettingsEntity mapResponseToEntity(final UserSettingsResponse response) {
        return UserSettingsEntity.builder()
                .name(response.getName())
                .age(response.getAge())
                .height(response.getHeight())
                .currentWeight(response.getCurrentWeight())
                .activity(response.getActivity())
                .birthday(DateUtil.format(response.getBirthday()))
                .gender(response.getGender())
                .goalProtein(response.getGoalProtein())
                .goalFat(response.getGoalFat())
                .goalCarbs(response.getGoalCarbs())
                .build();
    }
    public static UserSettings mapEntityToModel(final UserSettingsEntity entity) {
        return UserSettings.builder()
                .name(entity.getName())
                .age(entity.getAge())
                .height(entity.getHeight())
                .currentWeight(entity.getCurrentWeight())
                .activity(entity.getActivity())
                .birthday(entity.getBirthday())
                .gender(entity.getGender())
                .goalProtein(entity.getGoalProtein())
                .goalFat(entity.getGoalFat())
                .goalCarbs(entity.getGoalCarbs())
                .build();
    }
}
