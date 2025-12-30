package com.capricedumardi.agent.core.config.jmx;

import com.capricedumardi.agent.core.buffers.BufferStats;
import com.capricedumardi.agent.core.buffers.BuffersFactory;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class LangaAgentMetricsRegistry extends StandardMBean implements LangaAgentMetricsMBeans {

  // --- Metrics "Push" (Aggregated events) ---
  // These metrics are updated by calls from AbstractBuffer
  private final AtomicLong flushCount = new AtomicLong(0);
  private final AtomicLong flushTotalDurationMs = new AtomicLong(0);
  private final AtomicLong flushLastTimestamp = new AtomicLong(0);

  // Erreurs
  private final AtomicLong errorsTotal = new AtomicLong(0);
  private final ConcurrentHashMap<String, AtomicLong> errorsByType = new ConcurrentHashMap<>();

  // Performance
  private final long startTime = System.currentTimeMillis();

  // Singleton standard
  private static final LangaAgentMetricsRegistry INSTANCE;
  static {
    try {
      INSTANCE = new LangaAgentMetricsRegistry();
    } catch (NotCompliantMBeanException e) {
      throw new RuntimeException(e);
    }
  }

  public static LangaAgentMetricsRegistry getInstance() { return INSTANCE; }

  private LangaAgentMetricsRegistry() throws NotCompliantMBeanException {
    super(LangaAgentMetricsMBeans.class);
    registerJMX();
  }

  private void registerJMX() {
    try {
      ObjectName name = new ObjectName("com.capricedumardi.agent:type=LangaAgentMetricsRegistry");
      ManagementFactory.getPlatformMBeanServer().registerMBean(this, name);
    } catch (Exception e) {
      System.err.println("JMX Error: " + e.getMessage());
    }
  }

  // =========================================================================
  // PULL PART: Get the truth from the source (BuffersFactory)
  // =========================================================================

  // Helper to avoid NullPointer if the buffers are not yet initialized
  private BufferStats getSafeLogStats() {
    return BuffersFactory.getLogBufferStats();
  }

  private BufferStats getSafeMetricStats() {
    return BuffersFactory.getMetricBufferStats();
  }

  @Override
  public long getLogBufferSize() {
    BufferStats stats = getSafeLogStats();
    return stats != null ? stats.getMainQueueSize() : 0;
  }

  @Override
  public long getLogBufferCapacity() {
    BufferStats stats = getSafeLogStats();
    return stats != null ? stats.getMainQueueCapacity() : 0;
  }

  @Override
  public long getLogBufferRetrySize() {
    BufferStats stats = getSafeLogStats();
    return stats != null ? stats.getRetryQueueSize() : 0;
  }

  @Override
  public long getLogBufferDropped() {
    BufferStats stats = getSafeLogStats();
    return stats != null ? stats.getTotalDropped() : 0;
  }

  @Override
  public double getLogBufferUtilization() {
    BufferStats stats = getSafeLogStats();
    if (stats == null || stats.getMainQueueCapacity() == 0) return 0.0;
    return ((double) stats.getMainQueueSize() / stats.getMainQueueCapacity()) * 100.0;
  }

  @Override
  public long getMetricBufferSize() {
    BufferStats stats = getSafeMetricStats();
    return stats != null ? stats.getMainQueueSize() : 0;
  }

  @Override
  public long getMetricBufferCapacity() {
    BufferStats stats = getSafeMetricStats();
    return stats != null ? stats.getMainQueueCapacity() : 0;
  }

  @Override
  public long getMetricBufferRetrySize() {
    BufferStats stats = getSafeMetricStats();
    return stats != null ? stats.getRetryQueueSize() : 0;
  }

  @Override
  public long getMetricBufferDropped() {
    BufferStats stats = getSafeMetricStats();
    return stats != null ? stats.getTotalDropped() : 0;
  }

  // --- Global totals (Aggregation of stats from both buffers) ---

  @Override
  public long getTotalLogsSent() {
    BufferStats stats = getSafeLogStats();
    return stats != null ? stats.getTotalFlushed() : 0;
  }

  @Override
  public long getTotalMetricsSent() {
    BufferStats stats = getSafeMetricStats();
    return stats != null ? stats.getTotalFlushed() : 0;
  }

  @Override
  public long getTotalSendFailures() {
    long logsFail = getSafeLogStats() != null ? getSafeLogStats().getTotalSendFailures() : 0;
    long metricsFail = getSafeMetricStats() != null ? getSafeMetricStats().getTotalSendFailures() : 0;
    return logsFail + metricsFail;
  }

  // =========================================================================
  // PUSH PART: Methods called by AbstractBuffer for events
  // =========================================================================

  public void recordFlush(long durationMs) {
    flushCount.incrementAndGet();
    flushTotalDurationMs.addAndGet(durationMs);
    flushLastTimestamp.set(System.currentTimeMillis());
  }

  public void recordError(String type) {
    errorsTotal.incrementAndGet();
    errorsByType.computeIfAbsent(type, k -> new AtomicLong(0)).incrementAndGet();
  }

  @Override
  public long getFlushCount() { return flushCount.get(); }

  @Override
  public double getFlushDurationAvg() {
    long count = flushCount.get();
    return count == 0 ? 0.0 : (double) flushTotalDurationMs.get() / count;
  }

  @Override
  public long getFlushLastTimestamp() { return flushLastTimestamp.get(); }

  @Override
  public long getAgentUptime() { return System.currentTimeMillis() - startTime; }

  @Override
  public Map<String, Long> getErrorsByType() {
    Map<String, Long> result = new HashMap<>();
    errorsByType.forEach((k, v) -> result.put(k, v.get()));
    return result;
  }
}