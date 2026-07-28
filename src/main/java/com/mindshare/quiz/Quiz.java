package com.mindshare.quiz;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public class Quiz {
    private int id;
    private String title;
    private int durationMinutes;
    private String startTime;

    public Quiz(int id, String title, int durationMinutes) {
        this.id = id;
        this.title = title;
        this.durationMinutes = durationMinutes;
    }
    public Quiz(int id, String title, int durationMinutes, String startTime) {
        this(id, title, durationMinutes);
        this.startTime = startTime;
    }
    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getDurationMinutes() { return durationMinutes; }

    public boolean hasDuration() {
        return durationMinutes > 0;
    }

    public int getRemainingSeconds() {
        if (!hasDuration() || startTime == null || startTime.isBlank()) return durationMinutes * 60;
        try {
            Instant start = OffsetDateTime.parse(startTime).toInstant();
            long end = start.plusSeconds(durationMinutes * 60L).getEpochSecond();
            return Math.max(1, (int) (end - Instant.now().getEpochSecond()));
        } catch (DateTimeParseException ignored) {
            try {
                LocalDateTime start = LocalDateTime.parse(startTime);
                long end = start.plusMinutes(durationMinutes).atZone(ZoneId.systemDefault()).toEpochSecond();
                return Math.max(1, (int) (end - Instant.now().getEpochSecond()));
            } catch (DateTimeParseException ignoredAgain) {
                return durationMinutes * 60;
            }
        }
    }

    @Override
    public String toString() { return title; }
}


