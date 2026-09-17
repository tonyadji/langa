package com.langa.backend.domain.applications.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationUsageTest {

    @Test
    void empty_shouldHaveZeroUsage() {
        ApplicationUsage usage = ApplicationUsage.empty();
        assertEquals(0, usage.totalLogBytes());
        assertEquals(0, usage.totalMetricBytes());
    }

    @Test
    void increaseLogBytes_shouldAddToTotal() {
        ApplicationUsage usage = ApplicationUsage.empty().increaseLogBytes(100);
        assertEquals(100, usage.totalLogBytes());
        assertEquals(0, usage.totalMetricBytes());
    }

    @Test
    void increaseTotalMetricBytes_shouldAddToTotal() {
        ApplicationUsage usage = ApplicationUsage.empty().increaseTotalMetricBytes(50);
        assertEquals(0, usage.totalLogBytes());
        assertEquals(50, usage.totalMetricBytes());
    }

    @Test
    void increases_shouldAccumulate() {
        ApplicationUsage usage = ApplicationUsage.empty().increaseLogBytes(100).increaseLogBytes(50);
        assertEquals(150, usage.totalLogBytes());
    }
}
