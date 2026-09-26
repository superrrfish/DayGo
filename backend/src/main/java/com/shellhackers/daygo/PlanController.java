package com.shellhackers.daygo;

import org.springframework.web.bind.annotation.*;

@RestController
public class PlanController {

    @PostMapping("/api/plan")
    public PlanResponse getPlan(@RequestBody PlanRequest request) {
        // TODO: replace with real calculation logic
        String fakeDepartureTime = "13:45";
        String message = "Leave by " + fakeDepartureTime + " to reach " + request.destination();
        return new PlanResponse(fakeDepartureTime, message);
    }
}