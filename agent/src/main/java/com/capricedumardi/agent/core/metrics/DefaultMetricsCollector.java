package com.capricedumardi.agent.core.metrics;

import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.model.MetricEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DefaultMetricsCollector implements MetricsCollector {
    private static final Logger log = LogManager.getLogger(DefaultMetricsCollector.class);

    @Override
    public void track(String methodName, long durationMillis, String status, String uri, String httpMethod, int httpStatus) {
        MetricEntry metricEntry = new MetricEntry(methodName, durationMillis, status,
                Instant.ofEpochMilli(System.currentTimeMillis()).toString());
        metricEntry.setHttpMethod(httpMethod);
        metricEntry.setHttpStatus(httpStatus);
        metricEntry.setUri(uri);
        log.trace("Adding entry to metric buffer:");
        BuffersFactory.getMetricBufferInstance().add(metricEntry);
        log.trace("[{}] {} executed in {} with status {}",
                metricEntry.getTimestamp(), methodName, durationMillis, status);
    }

}
