package com.shellhackers.daygo.controller;

import com.shellhackers.daygo.model.Event;
import com.shellhackers.daygo.model.EventResponse;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final Map<String, Event> events;

    public EventController(Map<String, Event> eventMap) {
        this.events = eventMap;
    }

    @PostMapping
    public EventResponse createEvent(@RequestBody Event event) {
        String id = UUID.randomUUID().toString();
        Event saved = new Event(id, event.title(), event.location(), event.startTime(), event.endTime(), event.mode());
        events.put(id, saved);
        return EventResponse.from(saved);
    }

    @GetMapping
    public Collection<EventResponse> getAllEvents() {
        return events.values().stream().map(EventResponse::from).toList();
    }

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable String id) {
        Event event = events.get(id);
        if (event == null) throw new NoSuchElementException("Event not found: " + id);
        return EventResponse.from(event);
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable String id) {
        events.remove(id);
    }
}