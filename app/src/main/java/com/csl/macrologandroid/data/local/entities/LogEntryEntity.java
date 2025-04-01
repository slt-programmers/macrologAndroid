package com.csl.macrologandroid.data.local.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Entity(tableName = "logentries", indices = {@Index(value = {"external_id"}, unique = true)})
public class LogEntryEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "external_id")
    private Long externalId;
    @ColumnInfo(name = "food_id")
    private Long foodId;
    @ColumnInfo(name = "portion_id")
    private Long portionId;
    private Double multiplier;
    private String day;
    private String meal;

}
