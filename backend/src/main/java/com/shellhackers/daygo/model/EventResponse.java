package com.shellhackers.daygo.model;

public record EventResponse(
        String id,
        String title,
        String location,
        String date,
        String startTime,
        String endTime,
        TransportMode mode,
        String status,
        String description
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.id(), event.title(), event.location(),
                event.date(),
                event.startTime(), event.endTime(), event.mode(),
                event.status(),
                event.description()
        );
    }
}