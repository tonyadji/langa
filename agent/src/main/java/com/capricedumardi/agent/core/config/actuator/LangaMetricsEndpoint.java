package com.capricedumardi.agent.core.config.actuator;

import com.capricedumardi.agent.core.config.jmx.LangaAgentMetricsRegistry;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.HashMap;
import java.util.LinkedHashMap; // Pour garder l'ordre d'affichage
import java.util.Map;

@Endpoint(id = "langaMetrics")
public class LangaMetricsEndpoint {

  // Use the Registry singleton (which now performs the "pull" from the buffers)
  private final LangaAgentMetricsRegistry registry = LangaAgentMetricsRegistry.getInstance();

  @ReadOperation
  public Map<String, Object> getMetrics() {
    Map<String, Object> data = new LinkedHashMap<>();

    // --- 1. Buffers (Logs vs Metrics) ---
    Map<String, Object> buffers = new LinkedHashMap<>();

    // Log Buffer Details
    Map<String, Object> logBuffer = new HashMap<>();
    logBuffer.put("size", registry.getLogBufferSize());
    logBuffer.put("capacity", registry.getLogBufferCapacity());
    logBuffer.put("retrySize", registry.getLogBufferRetrySize());
    logBuffer.put("dropped", registry.getLogBufferDropped());
    logBuffer.put("utilizationPct", registry.getLogBufferUtilization());
    buffers.put("logs", logBuffer);

    // Metric Buffer Details
    Map<String, Object> metricBuffer = new HashMap<>();
    metricBuffer.put("size", registry.getMetricBufferSize());
    metricBuffer.put("capacity", registry.getMetricBufferCapacity());
    metricBuffer.put("retrySize", registry.getMetricBufferRetrySize());
    metricBuffer.put("dropped", registry.getMetricBufferDropped());
    buffers.put("metrics", metricBuffer);

    data.put("buffers", buffers);

    // --- 2. Totals & Sender ---
    Map<String, Object> sender = new HashMap<>();
    sender.put("totalLogsSent", registry.getTotalLogsSent());
    sender.put("totalMetricsSent", registry.getTotalMetricsSent());
    sender.put("totalFailures", registry.getTotalSendFailures());

    // Flush stats
    sender.put("flushCount", registry.getFlushCount());
    sender.put("flushDurationAvgMs", registry.getFlushDurationAvg());
    sender.put("lastFlushTs", registry.getFlushLastTimestamp());

    data.put("sender", sender);

    // --- 3. Errors ---
    data.put("errors", registry.getErrorsByType());

    // --- 4. Agent Info ---
    data.put("uptimeMs", registry.getAgentUptime());

    return data;
  }
}