package com.langa.backend.domain.applications.valueobjects;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogEntryTest {

    @Test
    void getSizeInBytes_shouldBeGreaterThanBaseOverhead() {
        LogEntry entry = new LogEntry()
                .setAppKey("app-1")
                .setAccountKey("acc-1")
                .setMessage("hello world")
                .setLevel("INFO")
                .setLoggerName("com.example.Logger")
                .setTimestamp(Instant.now());

        assertTrue(entry.getSizeInBytes() > Entry.BASE_DOCUMENT_OVERHEAD);
    }

    @Test
    void getSizeInBytes_shouldHandleNullFields() {
        LogEntry entry = new LogEntry().setTimestamp(Instant.now());
        assertTrue(entry.getSizeInBytes() > 0);
    }

    @Test
    void getRetentionDuration_andUnit_shouldDelegateToRetentionPolicy() {
        LogEntry entry = new LogEntry().setRetention(new RetentionPolicy(7, ChronoUnit.DAYS, null));

        assertEquals(7, entry.getRetentionDuration());
        assertEquals(ChronoUnit.DAYS, entry.getRetentionUnit());
    }
}
