package com.capricedumardi.agent.core.metrics;

public interface MetricsCollector {

    void track(String methodName, long durationMillis, String status, String uri, String httpMethod, int httpStatus);
}
