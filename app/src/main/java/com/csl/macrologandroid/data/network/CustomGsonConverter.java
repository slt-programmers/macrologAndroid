package com.csl.macrologandroid.data.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.converter.gson.GsonConverterFactory;

public class CustomGsonConverter {

    private CustomGsonConverter() {
        // No arg constructor
    }

    public static GsonConverterFactory create() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return GsonConverterFactory.create(gson);
    }

}

