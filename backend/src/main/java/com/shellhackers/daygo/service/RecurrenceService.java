package com.shellhackers.daygo.service;

import com.shellhackers.daygo.model.Event;
import com.shellhackers.daygo.model.EventResponse;
import com.shellhackers.daygo.model.RecurrenceRule;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class RecurrenceService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int PAST_WINDOW_DAYS = 14;
    private static final int FUTURE_WINDOW_DAYS = 60;

    /** Expands stored masters + one-off events into concrete occurrences, applying overrides/cancellations. */
    public List<EventResponse> expand(Collection<Event> allEvents) {
        List<Event> masters = allEvents.stream().filter(e -> e.recurrence() != null).toList();
        List<Event> oneOffs = allEvents.stream()
                .filter(e -> e.recurrence() == null && e.recurrenceId() == null).toList();

        Map<String, Map<String, Event>> exceptionsByMaster = new HashMap<>();
        for (Event e : allEvents) {
            if (e.recurrenceId() != null) {
                exceptionsByMaster
                        .computeIfAbsent(e.recurrenceId(), k -> new HashMap<>())
                        .put(e.originalDate(), e);
            }
        }

        LocalDate windowStart = LocalDate.now().minusDays(PAST_WINDOW_DAYS);
        LocalDate windowEnd = LocalDate.now().plusDays(FUTURE_WINDOW_DAYS);

        List<EventResponse> result = new ArrayList<>();
        for (Event oneOff : oneOffs) result.add(EventResponse.from(oneOff));

        for (Event master : masters) {
            Map<String, Event> exceptions = exceptionsByMaster.getOrDefault(master.id(), Map.of());
            for (LocalDate occDate : occurrenceDates(master, windowStart, windowEnd)) {
                String dateStr = occDate.format(DATE_FORMAT);
                String occId = master.id() + "::" + dateStr;
                Event exception = exceptions.get(dateStr);
                if (exception != null) {
                    if (exception.canceled()) continue;
                    result.add(EventResponse.forOccurrence(exception, occId, master.id(), master.recurrence()));
                } else {
                    Event occurrence = new Event(master.id(), master.title(), master.location(), dateStr,
                            master.startTime(), master.endTime(), master.mode(), master.description(),
                            null, null, null, false);
                    result.add(EventResponse.forOccurrence(occurrence, occId, master.id(), master.recurrence()));
                }
            }
        }
        return result;
    }

    private List<LocalDate> occurrenceDates(Event master, LocalDate windowStart, LocalDate windowEnd) {
        RecurrenceRule rule = master.recurrence();
        LocalDate seriesStart = master.date() != null
                ? LocalDate.parse(master.date(), DATE_FORMAT)
                : LocalDate.now();
        LocalDate seriesEnd = (rule.until() != null && !rule.until().isBlank())
                ? LocalDate.parse(rule.until(), DATE_FORMAT)
                : windowEnd;
        LocalDate effectiveEnd = seriesEnd.isBefore(windowEnd) ? seriesEnd : windowEnd;
        int interval = (rule.interval() == null || rule.interval() < 1) ? 1 : rule.interval();

        List<LocalDate> dates = new ArrayList<>();
        if (seriesStart.isAfter(effectiveEnd)) return dates;

        switch (rule.frequency()) {
            case DAILY -> {
                LocalDate d = seriesStart;
                while (!d.isAfter(effectiveEnd)) {
                    if (!d.isBefore(windowStart)) dates.add(d);
                    d = d.plusDays(interval);
                }
            }
            case WEEKLY -> {
                Set<DayOfWeek> days = (rule.daysOfWeek() == null || rule.daysOfWeek().isEmpty())
                        ? Set.of(seriesStart.getDayOfWeek())
                        : rule.daysOfWeek();
                LocalDate weekCursor = seriesStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                int weekCount = 0;
                while (!weekCursor.isAfter(effectiveEnd)) {
                    if (weekCount % interval == 0) {
                        for (DayOfWeek dow : days) {
                            LocalDate candidate = weekCursor.plusDays(dow.getValue() - 1);
                            if (!candidate.isBefore(seriesStart) && !candidate.isAfter(effectiveEnd)
                                    && !candidate.isBefore(windowStart)) {
                                dates.add(candidate);
                            }
                        }
                    }
                    weekCursor = weekCursor.plusWeeks(1);
                    weekCount++;
                }
            }
            case MONTHLY -> {
                LocalDate d = seriesStart;
                while (!d.isAfter(effectiveEnd)) {
                    if (!d.isBefore(windowStart)) dates.add(d);
                    d = d.plusMonths(interval);
                }
            }
        }
        Collections.sort(dates);
        return dates;
    }
}