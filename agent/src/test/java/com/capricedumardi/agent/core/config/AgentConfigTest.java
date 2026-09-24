package com.capricedumardi.agent.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentConfigTest {

    @Test
    void builderAppliesDefaultsWhenNothingIsSet() {
        AgentConfig config = new AgentConfig.Builder().build();

        assertEquals(50, config.getBatchSize());
        assertEquals(5, config.getFlushIntervalSeconds());
        assertEquals(10000, config.getMainQueueCapacity());
        assertEquals(5000, config.getRetryQueueCapacity());
        assertEquals(3, config.getHttpMaxRetryAttempts());
        assertEquals(5, config.getCircuitBreakerFailureThreshold());
        assertNull(config.getLoggingFramework(), "null (not 'none') means 'not configured', so auto-detection can still kick in");
        assertFalse(config.isDebugMode());
        assertFalse(config.isHttpCompressionEnabled());
        assertTrue(config.isKafkaEnableIdempotence());
        assertTrue(config.isKafkaAsyncSend());
        assertEquals("all", config.getKafkaAcks());
        assertEquals("snappy", config.getKafkaCompressionType());
        assertEquals("langa-agent-v1.0.0", config.getAgentVersion());
    }

    @Test
    void builderAppliesOverrides() {
        AgentConfig config = new AgentConfig.Builder()
                .ingestionUrl("https://host/api/ingestion/http/abc")
                .secret("s3cr3t")
                .loggingFramework("logback")
                .batchSize(10)
                .flushIntervalSeconds(1)
                .mainQueueCapacity(100)
                .retryQueueCapacity(50)
                .httpCompressionEnabled(true)
                .httpMaxRetryAttempts(7)
                .circuitBreakerFailureThreshold(9)
                .circuitBreakerOpenDurationMillis(12345L)
                .kafkaAcks("1")
                .kafkaAsyncSend(false)
                .debugMode(true)
                .build();

        assertEquals("https://host/api/ingestion/http/abc", config.getIngestionUrl());
        assertEquals("s3cr3t", config.getSecret());
        assertEquals("logback", config.getLoggingFramework());
        assertEquals(10, config.getBatchSize());
        assertEquals(1, config.getFlushIntervalSeconds());
        assertEquals(100, config.getMainQueueCapacity());
        assertEquals(50, config.getRetryQueueCapacity());
        assertTrue(config.isHttpCompressionEnabled());
        assertEquals(7, config.getHttpMaxRetryAttempts());
        assertEquals(9, config.getCircuitBreakerFailureThreshold());
        assertEquals(12345L, config.getCircuitBreakerOpenDurationMillis());
        assertEquals("1", config.getKafkaAcks());
        assertFalse(config.isKafkaAsyncSend());
        assertTrue(config.isDebugMode());
    }

    @Test
    void toStringIncludesKeyFields() {
        AgentConfig config = new AgentConfig.Builder().agentVersion("v9.9.9").build();

        String s = config.toString();
        assertTrue(s.contains("v9.9.9"));
        assertTrue(s.contains("batchSize"));
    }
}
