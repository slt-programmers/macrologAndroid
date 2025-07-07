package com.csl.macrologandroid.data.local.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.csl.macrologandroid.models.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Entity(tableName = "usersettings", indices = {@Index(value = {"external_id"}, unique = true)})
public class UserSettingsEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "external_id")
    private Long externalId;
    private String name;
    private String birthday;
    private Gender gender;
    private Integer height;
    @ColumnInfo(name = "current_weight")
    private Double currentWeight;
    private Double activity;
    @ColumnInfo(name = "goal_protein")
    private Integer goalProtein;
    @ColumnInfo(name = "goal_fat")
    private Integer goalFat;
    @ColumnInfo(name = "goal_carbs")
    private Integer goalCarbs;

}
