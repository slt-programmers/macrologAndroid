package com.csl.macrologandroid.cache;

import com.csl.macrologandroid.dtos.FoodDto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class FoodCache {

    private static FoodCache instance;

    private List<FoodDto> cache;

    private FoodCache() {
        this.cache = new ArrayList<>();
    }

    public static FoodCache getInstance() {
        if (instance == null) {
            instance = new FoodCache();
        }
        return instance;
    }

    public void addToCache(List<FoodDto> foodRespons) {
        cache.addAll(foodRespons);
    }

    public void clearCache() {
        cache = new ArrayList<>();
    }

}
