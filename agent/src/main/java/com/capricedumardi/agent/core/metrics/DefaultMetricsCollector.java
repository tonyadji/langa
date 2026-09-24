package com.capricedumardi.agent.core.metrics;

import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.config.LangaPrinter;
import com.capricedumardi.agent.core.model.MetricEntry;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DefaultMetricsCollector implements MetricsCollector {

    @Override
    public void track(String methodName, String signature, long durationMillis, String status, String uri, String httpMethod, int httpStatus) {
        MetricEntry metricEntry = new MetricEntry(methodName, signature, durationMillis, status,
                Instant.ofEpochMilli(System.currentTimeMillis()).toString());
        metricEntry.setHttpMethod(httpMethod);
        metricEntry.setHttpStatus(httpStatus);
        metricEntry.setUri(uri);
        LangaPrinter.printTrace("Adding entry to metric buffer:");
        BuffersFactory.getMetricBufferInstance().add(metricEntry);
        LangaPrinter.printTrace(metricEntry.getTimestamp()+" - "+ methodName);
    }

}
