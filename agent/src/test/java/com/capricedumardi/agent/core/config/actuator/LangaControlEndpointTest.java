package com.capricedumardi.agent.core.config.actuator;

import com.capricedumardi.agent.testsupport.AgentTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LangaControlEndpointTest {

    private final LangaControlEndpoint endpoint = new LangaControlEndpoint();

    @AfterEach
    void restoreDefaults() {
        AgentTestSupport.dynamicConfig().setBufferBatchSize(50);
        AgentTestSupport.dynamicConfig().setHttpCompressionThresholdBytes(1024);
        AgentTestSupport.dynamicConfig().setDebugMode(false);
    }

    @Test
    void getConfigExposesDynamicAndStaticFields() {
        Map<String, Object> config = endpoint.getConfig();

        assertTrue(config.containsKey("batchSize"));
        assertTrue(config.containsKey("flushIntervalSeconds"));
        assertTrue(config.containsKey("debugMode"));
        assertTrue(config.containsKey("compressionThreshold"));
        assertTrue(config.containsKey("agentVersion"));
        assertTrue(config.containsKey("ingestionUrl"));
        assertTrue(config.containsKey("loggingFramework"));
    }

    @Test
    void updateConfigAppliesProvidedValues() {
        endpoint.updateConfig(42, null, true, 2048);

        Map<String, Object> config = endpoint.getConfig();
        assertEquals(42, config.get("batchSize"));
        assertEquals(true, config.get("debugMode"));
        assertEquals(2048, config.get("compressionThreshold"));
    }

    @Test
    void updateConfigIgnoresNullValues() {
        AgentTestSupport.dynamicConfig().setBufferBatchSize(15);

        endpoint.updateConfig(null, null, null, null);

        assertEquals(15, endpoint.getConfig().get("batchSize"));
    }

    @Test
    void updateConfigIgnoresNonPositiveFlushIntervalAndThreshold() {
        AgentTestSupport.dynamicConfig().setHttpCompressionThresholdBytes(999);

        endpoint.updateConfig(null, 0, null, -5);

        assertEquals(999, endpoint.getConfig().get("compressionThreshold"));
    }
}
