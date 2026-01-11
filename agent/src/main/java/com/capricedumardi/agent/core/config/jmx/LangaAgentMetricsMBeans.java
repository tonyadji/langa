package com.capricedumardi.agent.core.config.jmx;

import java.util.Map;

public interface LangaAgentMetricsMBeans {

  // --- 1. Log Buffer (Pull depuis BufferFactory) ---
  long getLogBufferSize();
  long getLogBufferCapacity();
  long getLogBufferRetrySize();
  long getLogBufferDropped();
  double getLogBufferUtilization();

  // --- 2. Metric Buffer (Pull depuis BufferFactory) ---
  long getMetricBufferSize();
  long getMetricBufferCapacity();
  long getMetricBufferRetrySize();
  long getMetricBufferDropped();

  // --- 3. Sender / Flush (Global ou Split) ---
  long getFlushCount();
  double getFlushDurationAvg();
  long getFlushLastTimestamp();

  // --- 4. Totals ---
  long getTotalLogsSent();
  long getTotalMetricsSent();
  long getTotalSendFailures();

  // --- 5. General ---
  long getAgentUptime();
  Map<String, Long> getErrorsByType();
}