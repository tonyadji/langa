package com.capricedumardi.agent.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelClassesTest {

    @Test
    void logEntryShortConstructorLeavesOptionalFieldsNull() {
        LogEntry entry = new LogEntry("msg", "INFO", "logger", "2024-01-01T00:00:00Z");

        assertEquals("msg", entry.getMessage());
        assertEquals("INFO", entry.getLevel());
        assertEquals("logger", entry.getLoggerName());
        assertEquals("2024-01-01T00:00:00Z", entry.getTimestamp());
        assertNull(entry.getThreadName());
        assertNull(entry.getStackTrace());
        assertNull(entry.getMdc());
    }

    @Test
    void logEntryFullConstructorMakesMdcUnmodifiable() {
        Map<String, String> mdc = new java.util.HashMap<>();
        mdc.put("traceId", "abc");

        LogEntry entry = new LogEntry("msg", "ERROR", "logger", "ts", "thread-1", "stack", mdc);

        assertEquals("thread-1", entry.getThreadName());
        assertEquals("stack", entry.getStackTrace());
        assertEquals("abc", entry.getMdc().get("traceId"));
        assertThrows(UnsupportedOperationException.class, () -> entry.getMdc().put("x", "y"));
    }

    @Test
    void logEntryToStringContainsMessage() {
        LogEntry entry = new LogEntry("hello", "INFO", "logger", "ts");
        assertTrue(entry.toString().contains("hello"));
    }

    @Test
    void metricEntrySettersAndGettersRoundTrip() {
        MetricEntry entry = new MetricEntry("method", "sig", 42L, "SUCCESS", "ts");

        entry.setUri("/api/x");
        entry.setHttpMethod("GET");
        entry.setHttpStatus(200);

        assertEquals("method", entry.getName());
        assertEquals("sig", entry.getSignature());
        assertEquals(42L, entry.getDurationMillis());
        assertEquals("SUCCESS", entry.getStatus());
        assertEquals("ts", entry.getTimestamp());
        assertEquals("/api/x", entry.getUri());
        assertEquals("GET", entry.getHttpMethod());
        assertEquals(200, entry.getHttpStatus());
    }

    @Test
    void metricEntryRemainingSettersRoundTrip() {
        MetricEntry entry = new MetricEntry("method", "sig", 1L, "SUCCESS", "ts");

        entry.setName("renamed");
        entry.setSignature("newSig");
        entry.setDurationMillis(99L);
        entry.setStatus("ERROR");
        entry.setTimestamp("2024-02-02T00:00:00Z");

        assertEquals("renamed", entry.getName());
        assertEquals("newSig", entry.getSignature());
        assertEquals(99L, entry.getDurationMillis());
        assertEquals("ERROR", entry.getStatus());
        assertEquals("2024-02-02T00:00:00Z", entry.getTimestamp());
    }

    @Test
    void logRequestDtoExposesComponents() {
        LogEntry entry = new LogEntry("m", "INFO", "l", "ts");
        LogRequestDto dto = new LogRequestDto("app", "acc", List.of(entry), SendableRequestType.LOG);

        assertEquals("app", dto.appKey());
        assertEquals("acc", dto.accountKey());
        assertEquals(1, dto.entries().size());
        assertEquals(SendableRequestType.LOG, dto.type());
    }

    @Test
    void metricRequestDtoExposesComponents() {
        MetricEntry entry = new MetricEntry("m", "s", 1L, "SUCCESS", "ts");
        MetricRequestDto dto = new MetricRequestDto("app", "acc", List.of(entry), SendableRequestType.METRIC);

        assertEquals("app", dto.appKey());
        assertEquals(1, dto.entries().size());
        assertEquals(SendableRequestType.METRIC, dto.type());
    }

    @Test
    void senderTypeHasHttpAndKafka() {
        assertEquals(2, SenderType.values().length);
        assertEquals(SenderType.HTTP, SenderType.valueOf("HTTP"));
        assertEquals(SenderType.KAFKA, SenderType.valueOf("KAFKA"));
    }

    @Test
    void sendableRequestTypeHasLogAndMetric() {
        assertEquals(SendableRequestType.LOG, SendableRequestType.valueOf("LOG"));
        assertEquals(SendableRequestType.METRIC, SendableRequestType.valueOf("METRIC"));
    }
}
