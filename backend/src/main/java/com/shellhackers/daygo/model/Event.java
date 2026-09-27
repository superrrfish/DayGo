package com.shellhackers.daygo.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record Event(
        String id,
        String title,
        String location,
        String date,
        String startTime,
        String endTime,
        TransportMode mode,
        String description,
        RecurrenceRule recurrence,
        String recurrenceId,
        String originalDate,
        @JsonSetter(nulls = Nulls.AS_EMPTY) boolean canceled
) {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String status() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate eventDate = date != null
                ? LocalDate.parse(date, DATE_FORMAT)
                : LocalDate.now();

        LocalTime start = LocalTime.parse(startTime, TIME_FORMAT);
        LocalTime end   = endTime != null ? LocalTime.parse(endTime, TIME_FORMAT) : start;

        LocalDateTime startDt = LocalDateTime.of(eventDate, start);
        LocalDateTime endDt   = LocalDateTime.of(eventDate, end);

        if (now.isBefore(startDt)) return "upcoming";
        if (now.isAfter(endDt))   return "past";
        return "active";
    }
}