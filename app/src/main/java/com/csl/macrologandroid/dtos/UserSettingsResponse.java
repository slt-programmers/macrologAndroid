package com.csl.macrologandroid.dtos;

import com.csl.macrologandroid.models.Gender;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSettingsResponse implements Serializable {

    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("age")
    private int age;

    @Expose
    @SerializedName("birthday")
    private Date birthday;

    @Expose
    @SerializedName("gender")
    private Gender gender;

    @Expose
    @SerializedName("height")
    private int height;

    @Expose
    @SerializedName("currentWeight")
    private double currentWeight;

    @Expose
    @SerializedName("activity")
    private double activity;

    @Expose
    @SerializedName("goalProtein")
    private int goalProtein;

    @Expose
    @SerializedName("goalFat")
    private int goalFat;

    @Expose
    @SerializedName("goalCarbs")
    private int goalCarbs;

    public UserSettingsResponse() {
        // Non arg constructor
    }

 }
