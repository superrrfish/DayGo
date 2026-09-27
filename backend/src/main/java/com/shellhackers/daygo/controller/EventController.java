package com.shellhackers.daygo.controller;

import com.shellhackers.daygo.model.Event;
import com.shellhackers.daygo.model.EventResponse;
import com.shellhackers.daygo.service.RecurrenceService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final Map<String, Event> events;
    private final RecurrenceService recurrenceService;

    public EventController(Map<String, Event> eventMap, RecurrenceService recurrenceService) {
        this.events = eventMap;
        this.recurrenceService = recurrenceService;
    }

    @PostMapping
    public EventResponse createEvent(@RequestBody Event event) {
        String id = UUID.randomUUID().toString();
        String date = (event.date() != null && !event.date().isBlank())
                ? event.date()
                : LocalDate.now().toString();
        Event saved = new Event(id, event.title(), event.location(), date,
                event.startTime(), event.endTime(), event.mode(), event.description(),
                event.recurrence(), null, null, false);
        events.put(id, saved);
        return EventResponse.from(saved);
    }

    @GetMapping
    public Collection<EventResponse> getAllEvents() {
        return recurrenceService.expand(events.values());
    }

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable String id) {
        return EventResponse.from(resolve(id));
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable String id) {
        OccurrenceId occ = OccurrenceId.parse(id);
        if (occ != null) {
            Event master = events.get(occ.masterId());
            if (master == null) throw new NoSuchElementException("Series not found: " + occ.masterId());
            Event cancellation = new Event(id, master.title(), master.location(), occ.date(),
                    master.startTime(), master.endTime(), master.mode(), master.description(),
                    null, occ.masterId(), occ.date(), true);
            events.put(id, cancellation);
        } else {
            if (!events.containsKey(id)) throw new NoSuchElementException("Event not found: " + id);
            events.remove(id);
            events.values().removeIf(e -> id.equals(e.recurrenceId())); // drop this series' exceptions too
        }
    }

    @PutMapping("/{id}")
    public EventResponse updateEvent(@PathVariable String id, @RequestBody Event event) {
        OccurrenceId occ = OccurrenceId.parse(id);
        if (occ != null) {
            Event master = events.get(occ.masterId());
            if (master == null) throw new NoSuchElementException("Series not found: " + occ.masterId());
            Event override = new Event(id, event.title(), event.location(),
                    event.date() != null && !event.date().isBlank() ? event.date() : occ.date(),
                    event.startTime(), event.endTime(), event.mode(), event.description(),
                    null, occ.masterId(), occ.date(), false);
            events.put(id, override);
            return EventResponse.forOccurrence(override, id, occ.masterId(), master.recurrence());
        } else {
            if (!events.containsKey(id)) throw new NoSuchElementException("Event not found: " + id);
            Event updated = new Event(id, event.title(), event.location(),
                    event.date() != null && !event.date().isBlank() ? event.date() : LocalDate.now().toString(),
                    event.startTime(), event.endTime(), event.mode(), event.description(),
                    event.recurrence(), null, null, false);
            events.put(id, updated);
            return EventResponse.from(updated);
        }
    }

    @PostMapping("/import")
    public List<EventResponse> importEvents(@RequestBody List<Event> incoming) {
        List<EventResponse> created = new ArrayList<>();
        for (Event event : incoming) {
            created.add(createEvent(event));
        }
        return created;
    }

    /** Resolves a plain event id or a "masterId::date" occurrence id to a concrete Event. */
    private Event resolve(String id) {
        OccurrenceId occ = OccurrenceId.parse(id);
        if (occ == null) {
            Event event = events.get(id);
            if (event == null) throw new NoSuchElementException("Event not found: " + id);
            return event;
        }
        Event master = events.get(occ.masterId());
        if (master == null) throw new NoSuchElementException("Series not found: " + occ.masterId());
        Event exception = events.get(id);
        if (exception != null) {
            if (exception.canceled()) throw new NoSuchElementException("Occurrence was deleted: " + id);
            return exception;
        }
        return new Event(occ.masterId(), master.title(), master.location(), occ.date(),
                master.startTime(), master.endTime(), master.mode(), master.description(),
                null, null, null, false);
    }

    private record OccurrenceId(String masterId, String date) {
        static OccurrenceId parse(String id) {
            int idx = id.indexOf("::");
            if (idx < 0) return null;
            return new OccurrenceId(id.substring(0, idx), id.substring(idx + 2));
        }
    }


}