package com.capricedumardi.agent.core.metrics;

import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.model.MetricRequestDto;
import com.capricedumardi.agent.testsupport.AgentTestSupport;
import com.capricedumardi.agent.testsupport.RecordingSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultMetricsCollectorTest {

    @BeforeEach
    void resetSharedState() {
        AgentTestSupport.ensureBuffersFactoryBootstrapped();
        AgentTestSupport.bootstrapSender().reset();
    }

    @Test
    void trackAddsAnEntryToTheSharedMetricBuffer() {
        DefaultMetricsCollector collector = new DefaultMetricsCollector();

        collector.track("myMethod", "com.foo.Bar#myMethod()", 42L, "SUCCESS", "/api/x", "GET", 200);
        BuffersFactory.getMetricBufferInstance().flush();

        RecordingSenderService sender = AgentTestSupport.bootstrapSender();
        assertEquals(1, sender.sentPayloads().size());

        MetricRequestDto dto = (MetricRequestDto) sender.sentPayloads().get(0);
        assertEquals(1, dto.entries().size());
        var metric = dto.entries().get(0);
        assertEquals("myMethod", metric.getName());
        assertEquals(42L, metric.getDurationMillis());
        assertEquals("SUCCESS", metric.getStatus());
        assertEquals("/api/x", metric.getUri());
        assertEquals("GET", metric.getHttpMethod());
        assertEquals(200, metric.getHttpStatus());
    }

    @Test
    void trackWithoutHttpContextStillRecordsTheEntry() {
        DefaultMetricsCollector collector = new DefaultMetricsCollector();

        collector.track("bgJob", "com.foo.Bar#bgJob()", 5L, "SUCCESS", null, null, 0);
        BuffersFactory.getMetricBufferInstance().flush();

        RecordingSenderService sender = AgentTestSupport.bootstrapSender();
        assertTrue(sender.sentPayloads().size() >= 1);
    }
}
