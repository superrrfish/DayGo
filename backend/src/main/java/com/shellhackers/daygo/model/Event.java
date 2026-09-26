package com.shellhackers.daygo.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record Event(
        String id,
        String title,
        String location,
        String startTime,
        String endTime,
        TransportMode mode
) {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public String status() {
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.parse(startTime, TIME_FORMAT);
        LocalTime end = endTime != null ? LocalTime.parse(endTime, TIME_FORMAT) : start;

        if (now.isBefore(start)) return "upcoming";
        if (now.isAfter(end)) return "past";
        return "active";
    }
}