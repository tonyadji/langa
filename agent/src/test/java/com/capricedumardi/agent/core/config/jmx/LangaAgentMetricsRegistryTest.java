package com.capricedumardi.agent.core.config.jmx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LangaAgentMetricsRegistryTest {

    // Process-wide JMX singleton; other tests may have already pushed activity through
    // BuffersFactory, so assertions only check monotonic/sane behavior, not exact counts.

    @Test
    void getInstanceReturnsTheSameSingleton() {
        LangaAgentMetricsRegistry a = LangaAgentMetricsRegistry.getInstance();
        LangaAgentMetricsRegistry b = LangaAgentMetricsRegistry.getInstance();

        assertSame(a, b);
    }

    @Test
    void recordFlushIncreasesFlushCountAndAverageDuration() {
        LangaAgentMetricsRegistry registry = LangaAgentMetricsRegistry.getInstance();
        long before = registry.getFlushCount();

        registry.recordFlush(10);
        registry.recordFlush(20);

        assertTrue(registry.getFlushCount() >= before + 2);
        assertTrue(registry.getFlushDurationAvg() >= 0);
        assertTrue(registry.getFlushLastTimestamp() > 0);
    }

    @Test
    void recordErrorIncreasesErrorsByType() {
        LangaAgentMetricsRegistry registry = LangaAgentMetricsRegistry.getInstance();

        registry.recordError("TEST_ERROR_TYPE");

        assertTrue(registry.getErrorsByType().getOrDefault("TEST_ERROR_TYPE", 0L) >= 1);
    }

    @Test
    void agentUptimeIsPositive() {
        LangaAgentMetricsRegistry registry = LangaAgentMetricsRegistry.getInstance();

        assertTrue(registry.getAgentUptime() >= 0);
    }

    @Test
    void pullMetricsReturnNonNegativeValuesRegardlessOfBufferState() {
        LangaAgentMetricsRegistry registry = LangaAgentMetricsRegistry.getInstance();

        assertTrue(registry.getLogBufferSize() >= 0);
        assertTrue(registry.getLogBufferCapacity() >= 0);
        assertTrue(registry.getLogBufferRetrySize() >= 0);
        assertTrue(registry.getLogBufferDropped() >= 0);
        assertTrue(registry.getLogBufferUtilization() >= 0);
        assertTrue(registry.getMetricBufferSize() >= 0);
        assertTrue(registry.getMetricBufferCapacity() >= 0);
        assertTrue(registry.getMetricBufferRetrySize() >= 0);
        assertTrue(registry.getMetricBufferDropped() >= 0);
        assertTrue(registry.getTotalLogsSent() >= 0);
        assertTrue(registry.getTotalMetricsSent() >= 0);
        assertTrue(registry.getTotalSendFailures() >= 0);
    }
}
