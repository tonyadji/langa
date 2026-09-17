package com.capricedumardi.agent.core.buffers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BufferStatsTest {

    private BufferStats stats(long added, long flushed, long dropped, long retried, long failures,
                               int mainSize, int retrySize, int consecutiveErrors,
                               int mainCapacity, int retryCapacity) {
        return new BufferStats("buf", added, flushed, dropped, retried, failures,
                mainSize, retrySize, consecutiveErrors, mainCapacity, retryCapacity);
    }

    @Test
    void freshBufferWithNoActivityIsHealthy() {
        BufferStats s = stats(0, 0, 0, 0, 0, 0, 0, 0, 100, 100);

        assertEquals(0.0, s.getDropRate());
        assertEquals(1.0, s.getSuccessRate());
        assertEquals(0.0, s.getRetryRate());
        assertTrue(s.isHealthy());
        assertEquals("HEALTHY", s.getHealthStatus());
    }

    @Test
    void zeroCapacityDoesNotDivideByZero() {
        BufferStats s = stats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        assertEquals(0.0, s.getMainQueueFillPercentage());
        assertEquals(0.0, s.getRetryQueueFillPercentage());
    }

    @Test
    void highDropRateIsUnhealthy() {
        BufferStats s = stats(100, 90, 10, 0, 0, 0, 0, 0, 100, 100);

        assertEquals(0.10, s.getDropRate(), 0.0001);
        assertFalse(s.isHealthy());
    }

    @Test
    void criticalWhenDropRateAbove20Percent() {
        BufferStats s = stats(100, 50, 30, 0, 20, 0, 0, 0, 100, 100);

        assertTrue(s.getDropRate() > 0.20);
        assertEquals("CRITICAL", s.getHealthStatus());
    }

    @Test
    void criticalWhenSuccessRateBelow50Percent() {
        BufferStats s = stats(100, 30, 0, 0, 70, 0, 0, 0, 100, 100);

        assertTrue(s.getSuccessRate() < 0.50);
        assertEquals("CRITICAL", s.getHealthStatus());
    }

    @Test
    void degradedWhenUnhealthyButNotCritical() {
        BufferStats s = stats(100, 94, 6, 0, 0, 0, 0, 0, 100, 100);

        assertFalse(s.isHealthy());
        assertEquals("DEGRADED", s.getHealthStatus());
    }

    @Test
    void unhealthyWhenMainQueueOverEightyPercentFull() {
        BufferStats s = stats(10, 10, 0, 0, 0, 85, 0, 0, 100, 100);

        assertTrue(s.getMainQueueFillPercentage() > 0.80);
        assertFalse(s.isHealthy());
    }

    @Test
    void unhealthyWhenConsecutiveErrorsAtOrAboveThree() {
        BufferStats s = stats(10, 10, 0, 0, 0, 0, 0, 3, 100, 100);

        assertFalse(s.isHealthy());
    }

    @Test
    void gettersReturnConstructorValues() {
        BufferStats s = stats(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        assertEquals("buf", s.getBufferName());
        assertEquals(1, s.getTotalAdded());
        assertEquals(2, s.getTotalFlushed());
        assertEquals(3, s.getTotalDropped());
        assertEquals(4, s.getTotalRetried());
        assertEquals(5, s.getTotalSendFailures());
        assertEquals(6, s.getMainQueueSize());
        assertEquals(7, s.getRetryQueueSize());
        assertEquals(8, s.getConsecutiveErrors());
        assertEquals(9, s.getMainQueueCapacity());
        assertEquals(10, s.getRetryQueueCapacity());
    }

    @Test
    void toStringAndToCompactStringIncludeBufferName() {
        BufferStats s = stats(1, 1, 0, 0, 0, 0, 0, 0, 100, 100);

        assertTrue(s.toString().contains("buf"));
        assertTrue(s.toCompactString().contains("buf"));
    }
}
