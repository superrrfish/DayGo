package com.shellhackers.daygo.controller;

import com.shellhackers.daygo.model.PlanRequest;
import com.shellhackers.daygo.model.PlanResponse;
import com.shellhackers.daygo.service.RoutingService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@RestController
public class PlanController {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private final RoutingService routingService;

    public PlanController(RoutingService routingService) {
        this.routingService = routingService;
    }

    @PostMapping("/api/plan")
    public PlanResponse getPlan(@RequestBody PlanRequest request) {
        if (request.destination() == null || request.arrivalTime() == null || request.mode() == null) {
            throw new IllegalArgumentException("destination, arrivalTime, and mode are all required.");
        }

        LocalTime arrivalTime = LocalTime.parse(request.arrivalTime(), TIME_FORMAT);
        int travelMinutes = routingService.getTravelMinutes(request.origin(), request.destination(), request.mode());
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
}