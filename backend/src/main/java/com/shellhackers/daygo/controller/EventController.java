package com.shellhackers.daygo.controller;

import com.shellhackers.daygo.model.Event;
import com.shellhackers.daygo.model.EventResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
        String date = (event.date() != null && !event.date().isBlank())
                ? event.date()
                : LocalDate.now().toString();
        Event saved = new Event(id, event.title(), event.location(), date,
                event.startTime(), event.endTime(), event.mode(), event.description());
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

    @PutMapping("/{id}")
    public EventResponse updateEvent(@PathVariable String id, @RequestBody Event event) {
        if (!events.containsKey(id)) throw new NoSuchElementException("Event not found: " + id);
        Event updated = new Event(id, event.title(), event.location(),
                event.date() != null && !event.date().isBlank() ? event.date() : LocalDate.now().toString(),
                event.startTime(), event.endTime(), event.mode(), event.description());
        events.put(id, updated);
        return EventResponse.from(updated);
    }
}