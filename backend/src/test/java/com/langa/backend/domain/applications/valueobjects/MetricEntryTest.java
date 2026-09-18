package com.langa.backend.domain.applications.valueobjects;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricEntryTest {

    @Test
    void getSizeInBytes_shouldBeGreaterThanBaseOverhead() {
        MetricEntry entry = new MetricEntry()
                .setAppKey("app-1")
                .setAccountKey("acc-1")
                .setName("http.request")
                .setDurationMillis(120)
                .setStatus("SUCCESS")
                .setTimestamp(Instant.now())
                .setUri("/api/test")
                .setHttpMethod("GET")
                .setHttpStatus(200);

        assertTrue(entry.getSizeInBytes() > Entry.BASE_DOCUMENT_OVERHEAD);
    }

    @Test
    void getSizeInBytes_shouldHandleNullFields() {
        MetricEntry entry = new MetricEntry().setTimestamp(Instant.now());
        assertTrue(entry.getSizeInBytes() > 0);
    }

    @Test
    void getRetentionDuration_andUnit_shouldDelegateToRetentionPolicy() {
        MetricEntry entry = new MetricEntry().setRetention(new RetentionPolicy(3, ChronoUnit.DAYS, null));

        assertEquals(3, entry.getRetentionDuration());
        assertEquals(ChronoUnit.DAYS, entry.getRetentionUnit());
    }
}
