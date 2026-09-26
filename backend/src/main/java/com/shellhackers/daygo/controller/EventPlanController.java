package com.shellhackers.daygo.controller;

import com.shellhackers.daygo.model.EventResponse;
import com.shellhackers.daygo.model.PlanRequest;
import com.shellhackers.daygo.model.PlanResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
class EventPlanController {

    private final EventController eventController;
    private final PlanController planController;

    EventPlanController(EventController eventController, PlanController planController) {
        this.eventController = eventController;
        this.planController = planController;
    }

    @PostMapping("/{id}/plan")
    public PlanResponse planForEvent(@PathVariable String id,
                                     @RequestParam(required = false) Integer delayMinutes,
                                     @RequestParam(required = false) String origin) {
        EventResponse event = eventController.getEvent(id);
        PlanRequest request = new PlanRequest(event.location(), event.startTime(), event.mode(), delayMinutes, origin);
        return planController.getPlan(request);
    }
}