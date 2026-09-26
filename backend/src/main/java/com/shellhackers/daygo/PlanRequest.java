package com.shellhackers.daygo;

public record PlanRequest(
        String destination,
        String arrivalTime,
        TransportMode mode,
        Integer delayMinutes // optional — null or omitted means no delay
) {}