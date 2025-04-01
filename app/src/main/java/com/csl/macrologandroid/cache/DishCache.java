package com.csl.macrologandroid.cache;

import com.csl.macrologandroid.dtos.DishDto;

import java.util.ArrayList;
import java.util.List;

public class DishCache {

    private static DishCache instance;

    private List<DishDto> cache;

    private DishCache() {
        this.cache = new ArrayList<>();
    }

    public static DishCache getInstance() {
        if (instance == null) {
            instance = new DishCache();
        }
        return instance;
    }

    public void addToCache(List<DishDto> dishRespons) {
        cache.addAll(dishRespons);
    }

    public List<DishDto> getCache() {
        return cache;
    }

    public void clearCache() {
        cache = new ArrayList<>();
    }

}
