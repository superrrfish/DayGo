package com.shellhackers.daygo.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record Event(
        String id,
        String title,
        String location,
        String date,      // yyyy-MM-dd, defaults to today if null on creation
        String startTime, // HH:mm
        String endTime,   // HH:mm, optional
        TransportMode mode
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