package com.capricedumardi.agent.core.config.jmx;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentDynamicConfigTest {

    // This is a JVM-wide singleton shared with every other test class: always leave it
    // back in a sane default state so unrelated tests aren't affected by whichever
    // values this class happened to exercise last.
    @AfterEach
    void restoreDefaults() {
        AgentDynamicConfig config = AgentDynamicConfig.getInstance();
        config.setBufferBatchSize(50);
        config.setBufferFlushIntervalSeconds(3600);
        config.enableHttpCompression(false);
        config.setHttpCompressionThresholdBytes(1024);
        config.setDebugMode(false);
    }

    // AgentDynamicConfig is a process-wide JMX singleton (private constructor, only
    // reachable via getInstance()), so this only exercises the mutable "dynamic" subset
    // and the read-only static passthroughs — never assumes a specific starting value.

    @Test
    void getInstanceReturnsTheSameSingletonEveryTime() {
        AgentDynamicConfig a = AgentDynamicConfig.getInstance();
        AgentDynamicConfig b = AgentDynamicConfig.getInstance();

        assertNotNull(a);
        assertEquals(a, b);
    }

    @Test
    void bufferBatchSizeRoundTrips() {
        AgentDynamicConfig config = AgentDynamicConfig.getInstance();

        config.setBufferBatchSize(77);

        assertEquals(77, config.getBufferBatchSize());
    }

    @Test
    void invalidBufferBatchSizeIsIgnored() {
        AgentDynamicConfig config = AgentDynamicConfig.getInstance();
        config.setBufferBatchSize(10);

        config.setBufferBatchSize(0);
        config.setBufferBatchSize(-5);

        assertEquals(10, config.getBufferBatchSize());
    }

    @Test
    void bufferFlushIntervalRoundTrips() {
        AgentDynamicConfig config = AgentDynamicConfig.getInstance();

        config.setBufferFlushIntervalSeconds(120);

        assertEquals(120, config.getBufferFlushIntervalSeconds());
    }

    @Test
    void invalidFlushIntervalIsIgnored() {
        AgentDynamicConfig config = AgentDynamicConfig.getInstance();
        config.setBufferFlushIntervalSeconds(60);

        config.setBufferFlushIntervalSeconds(0);
        config.setBufferFlushIntervalSeconds(-1);

        assertEquals(60, config.getBufferFlushIntervalSeconds());
    }

    @Test
    void httpCompressionEnabledRoundTrips() {
        AgentDynamicConfig config = AgentDynamicConfig.getInstance();

        config.enableHttpCompression(true);
        assertTrue(config.isHttpCompressionEnabled());

        config.enableHttpCompression(false);
        assertEquals(false, config.isHttpCompressionEnabled());
    }

    @Test
    void httpCompressionThresholdRoundTrips() {
        AgentDynamicConfig config = AgentDynamicConfig.getInstance();

        config.setHttpCompressionThresholdBytes(2048);

        assertEquals(2048, config.getHttpCompressionThresholdBytes());
    }

    @Test
    void invalidHttpCompressionThresholdIsIgnored() {
        AgentDynamicConfig config = AgentDynamicConfig.getInstance();
        config.setHttpCompressionThresholdBytes(500);

        config.setHttpCompressionThresholdBytes(0);
        config.setHttpCompressionThresholdBytes(-1);

        assertEquals(500, config.getHttpCompressionThresholdBytes());
    }

    @Test
    void debugModeRoundTrips() {
        AgentDynamicConfig config = AgentDynamicConfig.getInstance();

        config.setDebugMode(true);
        assertTrue(config.isDebugMode());

        config.setDebugMode(false);
        assertEquals(false, config.isDebugMode());
    }

    @Test
    void staticPassthroughGettersReturnNonNullOrSaneValues() {
        AgentDynamicConfig config = AgentDynamicConfig.getInstance();

        assertNotNull(config.getAgentVersion());
        // getLoggingFramework() may legitimately be null: it means "not configured",
        // which is exactly what lets LangaAgentInitializer fall back to classpath
        // detection instead of an explicit LOGGING_FRAMEWORK=none/disabled.
        assertNotNull(config.getKafkaCompressionType());
        assertTrue(config.getMainQueueCapacity() >= 0);
        assertTrue(config.getHttpMaxConnectionsTotal() >= 0);
        assertTrue(config.getHttpMaxConnectionsPerRoute() >= 0);
        assertTrue(config.getHttpMaxRetryAttempts() >= 0);
        assertTrue(config.getHttpBaseRetryDelayMillis() >= 0);
        assertTrue(config.getHttpMaxRetryDelayMillis() >= 0);
        assertTrue(config.getHttpConnectionRequestTimeoutMillis() >= 0);
        assertTrue(config.getCircuitBreakerFailureThreshold() >= 0);
        assertTrue(config.getCircuitBreakerOpenDurationMillis() >= 0);
    }

    @Test
    void reloadConfigDoesNotThrow() {
        AgentDynamicConfig config = AgentDynamicConfig.getInstance();

        config.reloadConfig();
    }
}
