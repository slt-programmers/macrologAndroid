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
@Entity(tableName = "portions", indices = {@Index(value = {"external_id"}, unique = true)})
public class PortionEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "external_id")
    private Long externalId;
    @ColumnInfo(name = "food_id")
    private Long foodId;
    private Double grams;
    private String description;

}
