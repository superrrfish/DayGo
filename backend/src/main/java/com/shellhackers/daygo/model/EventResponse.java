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
        String description,
        boolean recurring,
        String seriesId,
        RecurrenceRule recurrence
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.id(), event.title(), event.location(),
                event.date(),
                event.startTime(), event.endTime(), event.mode(),
                event.status(),
                event.description(),
                event.recurrence() != null,
                event.recurrence() != null ? event.id() : null,
                event.recurrence()
        );
    }

    public static EventResponse forOccurrence(Event occurrenceData, String occurrenceId, String seriesId, RecurrenceRule recurrence) {
        return new EventResponse(
                occurrenceId, occurrenceData.title(), occurrenceData.location(),
                occurrenceData.date(),
                occurrenceData.startTime(), occurrenceData.endTime(), occurrenceData.mode(),
                occurrenceData.status(),
                occurrenceData.description(),
                true,
                seriesId,
                recurrence
        );
    }
}