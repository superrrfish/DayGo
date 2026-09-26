package com.shellhackers.daygo;

public record EventResponse(
        String id,
        String title,
        String location,
        String startTime,
        String endTime,
        TransportMode mode,
        String status
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.id(), event.title(), event.location(),
                event.startTime(), event.endTime(), event.mode(),
                event.status()
        );
    }
}