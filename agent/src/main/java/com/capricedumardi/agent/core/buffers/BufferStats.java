package com.capricedumardi.agent.core.buffers;

public class BufferStats {
    private final String bufferName;
    private final long totalAdded;
    private final long totalFlushed;
    private final long totalDropped;
    private final long totalRetried;
    private final long totalSendFailures;
    private final int mainQueueSize;
    private final int retryQueueSize;
    private final int consecutiveErrors;
    private final int mainQueueCapacity;
    private final int retryQueueCapacity;

    public BufferStats(String bufferName,
                       long totalAdded,
                       long totalFlushed,
                       long totalDropped,
                       long totalRetried,
                       long totalSendFailures,
                       int mainQueueSize,
                       int retryQueueSize,
                       int consecutiveErrors,
                       int mainQueueCapacity,
                       int retryQueueCapacity) {
        this.bufferName = bufferName;
        this.totalAdded = totalAdded;
        this.totalFlushed = totalFlushed;
        this.totalDropped = totalDropped;
        this.totalRetried = totalRetried;
        this.totalSendFailures = totalSendFailures;
        this.mainQueueSize = mainQueueSize;
        this.retryQueueSize = retryQueueSize;
        this.consecutiveErrors = consecutiveErrors;
        this.mainQueueCapacity = mainQueueCapacity;
        this.retryQueueCapacity = retryQueueCapacity;
    }

    public String getBufferName() { return bufferName; }

    public long getTotalAdded() { return totalAdded; }

    public long getTotalFlushed() { return totalFlushed; }

    public long getTotalDropped() { return totalDropped; }

    public long getTotalRetried() { return totalRetried; }

    public long getTotalSendFailures() { return totalSendFailures; }

    public int getMainQueueSize() { return mainQueueSize; }

    public int getRetryQueueSize() { return retryQueueSize; }

    public int getConsecutiveErrors() { return consecutiveErrors; }

    public int getMainQueueCapacity() { return mainQueueCapacity; }

    public int getRetryQueueCapacity() { return retryQueueCapacity; }

    public double getMainQueueFillPercentage() {
        if (mainQueueCapacity == 0) return 0.0;
        return (double) mainQueueSize / mainQueueCapacity;
    }

    public double getRetryQueueFillPercentage() {
        if (retryQueueCapacity == 0) return 0.0;
        return (double) retryQueueSize / retryQueueCapacity;
    }

    public double getDropRate() {
        if (totalAdded == 0) return 0.0;
        return (double) totalDropped / totalAdded;
    }

    public double getSuccessRate() {
        long totalAttempts = totalFlushed + totalSendFailures;
        if (totalAttempts == 0) return 1.0;
        return (double) totalFlushed / totalAttempts;
    }

    public double getRetryRate() {
        if (totalAdded == 0) return 0.0;
        return (double) totalRetried / totalAdded;
    }

    /**
     * Check if buffer is healthy.
     * A buffer is considered unhealthy if:
     * - Drop rate > 5%
     * - Success rate < 90%
     * - Main queue > 80% full
     * - Consecutive errors > 3
     */
    public boolean isHealthy() {
        return getDropRate() < 0.05 &&
                getSuccessRate() > 0.90 &&
                getMainQueueFillPercentage() < 0.80 &&
                consecutiveErrors < 3;
    }

    public String getHealthStatus() {
        if (isHealthy()) {
            return "HEALTHY";
        } else if (getDropRate() > 0.20 || getSuccessRate() < 0.50) {
            return "CRITICAL";
        } else {
            return "DEGRADED";
        }
    }

    @Override
    public String toString() {
        return String.format("""
            ========================================
            Buffer Statistics: %s
            ========================================
            Health Status: %s
            
            Throughput:
              Total Added:      %,d
              Total Flushed:    %,d
              Total Dropped:    %,d (%.2f%%)
              Total Retried:    %,d (%.2f%%)
            
            Queue Status:
              Main Queue:       %,d / %,d (%.1f%% full)
              Retry Queue:      %,d / %,d (%.1f%% full)
            
            Error Tracking:
              Send Failures:    %,d
              Success Rate:     %.2f%%
              Consecutive Errs: %d
            ========================================
            """,
                bufferName,
                getHealthStatus(),
                totalAdded,
                totalFlushed,
                totalDropped, getDropRate() * 100,
                totalRetried, getRetryRate() * 100,
                mainQueueSize, mainQueueCapacity, getMainQueueFillPercentage() * 100,
                retryQueueSize, retryQueueCapacity, getRetryQueueFillPercentage() * 100,
                totalSendFailures,
                getSuccessRate() * 100,
                consecutiveErrors
        );
    }

    public String toCompactString() {
        return String.format("[%s] %s | Added: %d, Flushed: %d, Dropped: %d, Queue: %d/%d (%.0f%%)",
                bufferName,
                getHealthStatus(),
                totalAdded,
                totalFlushed,
                totalDropped,
                mainQueueSize,
                mainQueueCapacity,
                getMainQueueFillPercentage() * 100
        );
    }
}
