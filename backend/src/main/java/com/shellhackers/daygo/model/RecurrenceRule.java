package com.shellhackers.daygo.model;

import java.time.DayOfWeek;
import java.util.Set;

public record RecurrenceRule(
        Frequency frequency,
        Integer interval,          // every N units; null/absent treated as 1
        Set<DayOfWeek> daysOfWeek, // WEEKLY only; null/empty = same weekday as start date
        String until                // yyyy-MM-dd, optional end date (inclusive); null = no explicit end
) {
    public enum Frequency { DAILY, WEEKLY, MONTHLY }
}