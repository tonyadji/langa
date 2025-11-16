package com.capricedumardi.agent.core.buffers;

import com.capricedumardi.agent.core.model.*;
import com.capricedumardi.agent.core.services.SenderService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BuffersFactory {
    private static volatile GenericBuffer<LogEntry, SendableRequestDto> logBufferInstance;
    private static volatile GenericBuffer<MetricEntry, SendableRequestDto> metricBufferInstance;
    private static volatile ScheduledExecutorService scheduler;
    private static volatile SenderService senderServiceInstance;

    private static final Object LOG_BUFFER_LOCK = new Object();
    private static final Object METRIC_BUFFER_LOCK = new Object();
    private static final Object SCHEDULER_LOCK = new Object();

    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    private static final int SHUTDOWN_TIMEOUT_SECONDS = 30;
    private static final int FORCED_SHUTDOWN_TIMEOUT_SECONDS = 10;

    private BuffersFactory() {
    }

    /**
     * Initialize all buffers and scheduler.
     * Thread-safe - can be called concurrently from multiple threads.
     * Subsequent calls after successful initialization are no-ops.
     *
     * @throws IllegalStateException if called after shutdown
     */
    public static void init(SenderService senderService, String appKey, String accountKey,
                            int batchSize, int flushIntervalSeconds) {

        if (shuttingDown.get()) {
            throw new IllegalStateException("Cannot initialize BuffersFactory after shutdown");
        }

        if (initialized.get()) {
            System.out.println("BuffersFactory already initialized, skipping re-initialization");
            return;
        }
        senderServiceInstance = senderService;
        initScheduler();

        initLogBuffer(senderService, appKey, accountKey, batchSize, flushIntervalSeconds);
        initMetricBuffer(senderService, appKey, accountKey, batchSize, flushIntervalSeconds);

        initialized.set(true);
        System.out.println("BuffersFactory initialized successfully");
    }

    /**
     * Get log buffer instance.
     * Thread-safe read (volatile).
     *
     * @throws IllegalStateException if not initialized
     */
    public static GenericBuffer<LogEntry, SendableRequestDto> getLogBufferInstance() {
        GenericBuffer<LogEntry, SendableRequestDto> instance = logBufferInstance;
        if (instance == null) {
            throw new IllegalStateException("LogBuffer instance is not initialized. Call init() first.");
        }
        return instance;
    }

    /**
     * Get metric buffer instance.
     * Thread-safe read (volatile).
     *
     * @throws IllegalStateException if not initialized
     */
    public static GenericBuffer<MetricEntry, SendableRequestDto> getMetricBufferInstance() {
        GenericBuffer<MetricEntry, SendableRequestDto> instance = metricBufferInstance;
        if (instance == null) {
            throw new IllegalStateException("MetricBuffer instance is not initialized. Call init() first.");
        }
        return instance;
    }

    /**
     * Get scheduler instance.
     * Thread-safe read (volatile).
     */
    public static ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    /**
     * Gracefully shutdown all buffers and scheduler.
     * Thread-safe and idempotent - can be called multiple times.
     *
     * Shutdown sequence:
     * 1. Mark as shutting down (prevent new operations)
     * 2. Flush all buffers (send remaining data)
     * 3. Shutdown scheduler gracefully
     * 4. Force shutdown if timeout exceeded
     */
    public static void shutdownAll() {
        if (!shuttingDown.compareAndSet(false, true)) {
            System.out.println("BuffersFactory already shutting down, skipping duplicate shutdown");
            return;
        }

        System.out.println("BuffersFactory: Starting graceful shutdown...");

        try {
            flushAllBuffers();

            shutdownScheduler();

            closeSenderService();

            System.out.println("Bufferstory shutdown complete");

        } catch (Exception e) {
            System.err.println("✗ Error during BuffersFactory shutdown: " + e.getMessage());
            e.printStackTrace(System.err);
        } finally {
            initialized.set(false);
        }
    }

    /**
     * Check if factory is initialized.
     * Thread-safe read.
     */
    public static boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Check if factory is shutting down.
     * Thread-safe read.
     */
    public static boolean isShuttingDown() {
        return shuttingDown.get();
    }

    /**
     * Get buffer statistics for monitoring.
     * Returns null if buffer not initialized.
     */
    public static BufferStats getLogBufferStats() {
        return logBufferInstance != null ? logBufferInstance.getStats() : null;
    }

    /**
     * Get buffer statistics for monitoring.
     * Returns null if buffer not initialized.
     */
    public static BufferStats getMetricBufferStats() {
        return metricBufferInstance != null ? metricBufferInstance.getStats() : null;
    }
    private static void flushAllBuffers() {
        System.out.println("Flushing all buffers...");

        if (logBufferInstance != null) {
            try {
                logBufferInstance.flush();
                System.out.println("Log buffer flushed");
            } catch (Exception e) {
                System.err.println("Error flushing log buffer: " + e.getMessage());
            }
        }

        if (metricBufferInstance != null) {
            try {
                metricBufferInstance.flush();
                System.out.println("Metric buffer flushed");
            } catch (Exception e) {
                System.err.println("Error flushing metric buffer: " + e.getMessage());
            }
        }
    }

    private static void initScheduler() {
        if (scheduler == null) {
            synchronized (SCHEDULER_LOCK) {
                if (scheduler == null) {
                    int threadPoolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);

                    scheduler = Executors.newScheduledThreadPool(
                            threadPoolSize,
                            runnable -> {
                                Thread thread = new Thread(runnable, "langa-agent-scheduler");
                                thread.setDaemon(true);
                                return thread;
                            }
                    );

                    System.out.println("Scheduler initialized with " + threadPoolSize + " threads");
                }
            }
        }
    }

    private static void initLogBuffer(SenderService senderService, String appKey, String accountKey,
                                      int batchSize, int flushIntervalSeconds) {
        if (logBufferInstance == null) {
            synchronized (LOG_BUFFER_LOCK) {
                if (logBufferInstance == null) {
                    logBufferInstance = new GenericBuffer<>(
                            entries -> new LogRequestDto(appKey, accountKey, entries, SendableRequestType.LOG),
                            senderService,
                            appKey,
                            accountKey,
                            batchSize,
                            flushIntervalSeconds,
                            "logBuffer"
                    );
                    System.out.println("Log buffer initialized");
                }
            }
        }
    }

    private static void initMetricBuffer(SenderService senderService, String appKey, String accountKey,
                                         int batchSize, int flushIntervalSeconds) {
        if (metricBufferInstance == null) {
            synchronized (METRIC_BUFFER_LOCK) {
                if (metricBufferInstance == null) {
                    metricBufferInstance = new GenericBuffer<>(
                            entries -> new MetricRequestDto(appKey, accountKey, entries, SendableRequestType.METRIC),
                            senderService,
                            appKey,
                            accountKey,
                            batchSize,
                            flushIntervalSeconds,
                            "metricBuffer"
                    );
                    System.out.println("Metric buffer initialized");
                }
            }
        }
    }

    private static void shutdownScheduler() {
        if (scheduler == null || scheduler.isShutdown()) {
            return;
        }

        System.out.println("Shutting down scheduler...");

        try {
            scheduler.shutdown();

            if (!scheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                System.err.println("Scheduler did not terminate gracefully within " +
                        SHUTDOWN_TIMEOUT_SECONDS + " seconds, forcing shutdown");

                scheduler.shutdownNow();

                if (!scheduler.awaitTermination(FORCED_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    System.err.println("Scheduler did not terminate after forced shutdown");
                } else {
                    System.out.println("Scheduler forcefully shutdown");
                }
            } else {
                System.out.println("✓ Scheduler shutdown gracefully");
            }

        } catch (InterruptedException e) {
            System.err.println("Interrupted during scheduler shutdown");
            scheduler.shutdownNow();
            // Restore interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private static void closeSenderService() {
        if (senderServiceInstance == null) {
            return;
        }

        System.out.println("Closing sender service...");

        try {
            senderServiceInstance.close();
            System.out.println("✓ Sender service closed");
        } catch (Exception e) {
            System.err.println("✗ Error closing sender service: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
