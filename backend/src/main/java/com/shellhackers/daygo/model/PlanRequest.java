package com.shellhackers.daygo.model;

public record PlanRequest(
        String destination,
        String arrivalTime,
        TransportMode mode,
        Integer delayMinutes,
        String origin // optional — where the user is coming from
) {}