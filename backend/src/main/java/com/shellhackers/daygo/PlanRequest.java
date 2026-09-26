package com.shellhackers.daygo;

public record PlanRequest(
        String destination,
        String arrivalTime,   // e.g. "14:30" or ISO format — decide with your teammate
        TransportMode mode
) {}