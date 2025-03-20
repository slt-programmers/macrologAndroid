package com.csl.macrologandroid.dtos;

import lombok.Data;

@Data
public class ConnectivityRequest {

    private String name;

    private String value;

    public ConnectivityRequest(String name, String value) {
        this.name = name;
        this.value = value;
    }

}
