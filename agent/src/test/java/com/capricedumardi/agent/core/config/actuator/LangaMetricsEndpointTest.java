package com.capricedumardi.agent.core.config.actuator;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LangaMetricsEndpointTest {

    @Test
    void getMetricsExposesBuffersSenderErrorsAndUptime() {
        LangaMetricsEndpoint endpoint = new LangaMetricsEndpoint();

        Map<String, Object> metrics = endpoint.getMetrics();

        assertTrue(metrics.containsKey("buffers"));
        assertTrue(metrics.containsKey("sender"));
        assertTrue(metrics.containsKey("errors"));
        assertTrue(metrics.containsKey("uptimeMs"));

        @SuppressWarnings("unchecked")
        Map<String, Object> buffers = (Map<String, Object>) metrics.get("buffers");
        assertTrue(buffers.containsKey("logs"));
        assertTrue(buffers.containsKey("metrics"));

        @SuppressWarnings("unchecked")
        Map<String, Object> sender = (Map<String, Object>) metrics.get("sender");
        assertTrue(sender.containsKey("totalLogsSent"));
        assertTrue(sender.containsKey("flushCount"));
    }
}
