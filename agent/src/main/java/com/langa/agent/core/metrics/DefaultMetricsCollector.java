package com.langa.agent.core.metrics;

import com.langa.agent.core.buffers.BuffersFactory;
import com.langa.agent.core.model.MetricEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.List;

public class DefaultMetricsCollector implements MetricsCollector {
    private static final Logger log = LogManager.getLogger(DefaultMetricsCollector.class);

    @Override
    public void track(String methodName, long durationMillis, String status) {
        MetricEntry metricEntry = new MetricEntry(methodName, durationMillis, status, Instant.now().toEpochMilli());
        BuffersFactory.getMetricBufferInstance().add(metricEntry);
        log.info("[{}] {} executed in {} with status {}",
                List.of(Instant.now(), methodName, durationMillis, status).toArray());
    }
}
