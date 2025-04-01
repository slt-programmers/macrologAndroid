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
@Entity(tableName = "dishes", indices = {@Index(value = {"external_id"}, unique = true)})
public class DishEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "external_id")
    private Long externalId;
    private String name;

}
