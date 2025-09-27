package com.langa.agent.core.metrics;

public interface MetricsCollector {

    void track(String methodName, long durationMillis, String status);
}
