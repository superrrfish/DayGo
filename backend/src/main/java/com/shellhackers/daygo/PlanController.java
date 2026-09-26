package com.shellhackers.daygo;

import org.springframework.web.bind.annotation.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@RestController
public class PlanController {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @PostMapping("/api/plan")
    public PlanResponse getPlan(@RequestBody PlanRequest request) {
        LocalTime arrivalTime = LocalTime.parse(request.arrivalTime(), TIME_FORMAT);

        int travelMinutes = estimateTravelMinutes(request.mode());
        int bufferMinutes = 5;
        int delayMinutes = request.delayMinutes() != null ? request.delayMinutes() : 0;

        LocalTime departureTime = arrivalTime.minusMinutes(travelMinutes + bufferMinutes + delayMinutes);

        String formattedDeparture = departureTime.format(TIME_FORMAT);

        String message = delayMinutes > 0
                ? "Delay detected (+" + delayMinutes + " min) - leave by " + formattedDeparture
                + " to reach " + request.destination()
                : "Leave by " + formattedDeparture + " to reach " + request.destination()
                + " via " + request.mode().name().toLowerCase();

        return new PlanResponse(formattedDeparture, message);
    }

    private int estimateTravelMinutes(TransportMode mode) {
        return switch (mode) {
            case CAR -> 20;
            case RIDESHARE -> 25;
            case TRANSIT -> 35;
            case BIKE -> 30;
            case WALK -> 50;
        };
    }
}