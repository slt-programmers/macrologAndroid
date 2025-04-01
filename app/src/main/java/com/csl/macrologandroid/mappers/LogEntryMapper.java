package com.csl.macrologandroid.mappers;

import android.util.Log;

import com.csl.macrologandroid.data.local.entities.LogEntryEntity;
import com.csl.macrologandroid.data.models.FoodData;
import com.csl.macrologandroid.data.models.LogEntryData;
import com.csl.macrologandroid.dtos.LogEntryRequest;
import com.csl.macrologandroid.dtos.LogEntryResponse;
import com.csl.macrologandroid.models.LogEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LogEntryMapper {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    @Deprecated
    public static LogEntryRequest mapResponseToRequest(final LogEntryResponse logEntryResponse) {
        return new LogEntryRequest(
                (long) logEntryResponse.getId(),
                logEntryResponse.getFood(),
                logEntryResponse.getPortion(),
                logEntryResponse.getMultiplier(),
                FORMAT.format(logEntryResponse.getDay()),
                logEntryResponse.getMeal().toString()
        );
    }

    public static List<LogEntryEntity> mapResponsesToEntities(final List<LogEntryResponse> responses,
                                                              final List<FoodData> foodDatas) {
        return responses.stream().map(response -> {
            final var food = foodDatas.stream().filter(f -> response.getFood().getId().equals(f.foodEntity.getExternalId())).toList().get(0);
            return mapResponseToEntity(response, food);
        }).toList();
    }

    public static LogEntryEntity mapResponseToEntity(final LogEntryResponse response, final FoodData foodData) {
        final var portionExternalId = response.getPortion() != null ? response.getPortion().getId() : null;
        final var portionEntity = portionExternalId != null ? foodData.portionEntities.stream().filter(p -> portionExternalId.equals(p.getExternalId())).toList().get(0) : null;
        return LogEntryEntity.builder()
                .externalId((long) response.getId())
                .day(FORMAT.format(response.getDay()))
                .meal(response.getMeal().name())
                .foodId(foodData.foodEntity.getId())
                .portionId(portionEntity != null ? portionEntity.getId() : null)
                .multiplier(response.getMultiplier())
                .build();
    }

    public static List<LogEntry> mapDatasToModels(final List<LogEntryData> datas) {
        if (datas == null) {
            return new ArrayList<>();
        }
        return datas.stream().map(LogEntryMapper::mapDataToModel).toList();
    }

    public static LogEntry mapDataToModel(final LogEntryData data) {
        final var food = FoodMapper.mapEntityToModel(data.foodForEntity, data.portionsForFood);
        final var portion = PortionMapper.mapEntityToModel(data.portionForEntity);
        return LogEntry.builder()
                .id(data.logEntryEntity.getId())
                .externalId(data.logEntryEntity.getExternalId())
                .day(data.logEntryEntity.getDay())
                .meal(data.logEntryEntity.getMeal())
                .food(food)
                .portion(portion)
                .build();
    }

    public static List<LogEntryRequest> mapDatasToRequests(final List<LogEntryData> datas) {
        if (datas == null) {
            return null;
        }
        return datas.stream().map(LogEntryMapper::mapDataToRequest).toList();
    }

    public static LogEntryRequest mapDataToRequest(final LogEntryData data) {
        final var food = FoodMapper.mapEntityToDto(data.foodForEntity, data.portionsForFood);
        final var portion = PortionMapper.mapEntityToDto(data.portionForEntity);
        return LogEntryRequest.builder()
                .id(data.logEntryEntity.getExternalId()) // external id is for requests/responses/network
                .multiplier(data.logEntryEntity.getMultiplier())
                .day(data.logEntryEntity.getDay())
                .meal(data.logEntryEntity.getMeal())
                .food(food)
                .portion(portion)
                .build();
    }
}
