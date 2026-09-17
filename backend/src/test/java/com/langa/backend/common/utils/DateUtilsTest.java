package com.langa.backend.common.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void fromTimestamp_shouldThrow_whenNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> DateUtils.fromTimestamp(null));
        assertThrows(IllegalArgumentException.class, () -> DateUtils.fromTimestamp("  "));
    }

    @Test
    void fromTimestamp_shouldParseUnixSeconds() {
        LocalDateTime result = DateUtils.fromTimestamp("1700000000");
        assertNotNull(result);
    }

    @Test
    void fromTimestamp_shouldParseUnixMillis() {
        LocalDateTime result = DateUtils.fromTimestamp("1700000000000");
        assertNotNull(result);
    }

    @Test
    void fromTimestamp_shouldParseFormattedDate() {
        LocalDateTime result = DateUtils.fromTimestamp("2025-01-15T10:30:00.123");
        assertEquals(2025, result.getYear());
        assertEquals(1, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
    }

    @Test
    void fromTimestamp_shouldParseIsoFallbackFormat() {
        LocalDateTime result = DateUtils.fromTimestamp("2025-01-15T10:30:00");
        assertEquals(2025, result.getYear());
    }

    @Test
    void fromTimestamp_shouldThrow_whenFormatUnrecognized() {
        assertThrows(IllegalArgumentException.class, () -> DateUtils.fromTimestamp("not-a-date"));
    }

    @Test
    void toFormattedString_shouldFormatUsingPattern() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 15, 10, 30, 0, 123_000_000);
        String result = DateUtils.toFormattedString(dateTime);
        assertEquals("2025-01-15T10:30:00.123", result);
    }

    @Test
    void toUnixTimestamp_shouldReturnPositiveEpochMillis() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 1, 15, 10, 30, 0);
        assertTrue(DateUtils.toUnixTimestamp(dateTime) > 0);
    }
}
