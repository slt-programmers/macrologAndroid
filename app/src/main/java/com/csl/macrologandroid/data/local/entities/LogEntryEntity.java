package com.csl.macrologandroid.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Data;


@Data
@Entity
public class LogEntryEntity {
    @PrimaryKey
    private Long id;
    private Long foodId;
    private Long portionId;
    private Double multiplier;
    private String day;
    private String meal;

}
