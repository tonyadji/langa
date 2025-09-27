package com.langa.agent.core.metrics;

import com.langa.agent.core.buffers.Buffers;
import com.langa.agent.core.model.MetricEntry;

import java.time.Instant;

public class DefaultMetricsCollector implements MetricsCollector {

    @Override
    public void track(String methodName, long durationMillis, String status) {
        MetricEntry metricEntry = new MetricEntry(methodName, durationMillis, status, Instant.now().toEpochMilli());
        Buffers.getMetricBufferInstance().add(metricEntry);
        System.out.printf("[%s] %s executed in %dms with status %s%n",
                Instant.now(), methodName, durationMillis, status);
    }
}
