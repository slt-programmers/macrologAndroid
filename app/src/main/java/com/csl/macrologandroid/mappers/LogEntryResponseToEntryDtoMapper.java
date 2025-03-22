package com.csl.macrologandroid.mappers;

import com.csl.macrologandroid.dtos.EntryDto;
import com.csl.macrologandroid.dtos.LogEntryResponse;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class LogEntryResponseToEntryDtoMapper {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public static EntryDto map(final LogEntryResponse logEntryResponse) {
        return new EntryDto(
                (long) logEntryResponse.getId(),
                logEntryResponse.getFood(),
                logEntryResponse.getPortion(),
                logEntryResponse.getMultiplier(),
                FORMAT.format(logEntryResponse.getDay()),
                logEntryResponse.getMeal().toString()
        );
    }
}
