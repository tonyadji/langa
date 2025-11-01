package com.capricedumardi.agent.core.buffers;

import com.capricedumardi.agent.core.model.LogEntry;
import com.capricedumardi.agent.core.model.LogRequestDto;
import com.capricedumardi.agent.core.model.MetricEntry;
import com.capricedumardi.agent.core.model.MetricRequestDto;
import com.capricedumardi.agent.core.model.SendableRequestDto;
import com.capricedumardi.agent.core.model.SendableRequestType;
import com.capricedumardi.agent.core.services.SenderService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class BuffersFactory {
    private static GenericBuffer<LogEntry, SendableRequestDto> logBufferInstance;
    private static GenericBuffer<MetricEntry, SendableRequestDto> metricBufferInstance;
    private static ScheduledExecutorService scheduler;

    private BuffersFactory() {}

    public static void init(SenderService senderService, String appKey, String accountKey, int batchSize, int flushIntervalSeconds) {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1, runnableScheduler -> new Thread(runnableScheduler, "langa-agent-scheduler"));
        }
        if( logBufferInstance == null) {
            logBufferInstance = new GenericBuffer<>(
                    entries -> new LogRequestDto(appKey, accountKey, entries, SendableRequestType.LOG),
                    senderService, appKey, accountKey, batchSize, flushIntervalSeconds);
        }
        if( metricBufferInstance == null) {
            metricBufferInstance = new GenericBuffer<>(
                    entries -> new MetricRequestDto(appKey, accountKey, entries, SendableRequestType.METRIC),
                    senderService, appKey, accountKey, batchSize, flushIntervalSeconds);
        }
    }

    public static GenericBuffer<LogEntry, SendableRequestDto> getLogBufferInstance() {
        if( logBufferInstance == null) {
            throw new IllegalStateException("LogBuffer instance is not initialized");
        }
        return logBufferInstance;
    }

    public static GenericBuffer<MetricEntry, SendableRequestDto> getMetricBufferInstance() {
        if( metricBufferInstance == null) {
            throw new IllegalStateException("MetricBuffer instance is not initialized");
        }
        return metricBufferInstance;
    }

    public static void shutdownAll() {
        if (logBufferInstance != null) {
            logBufferInstance.flush();
        }
        if (metricBufferInstance != null) {
            metricBufferInstance.flush();
        }

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    public static ScheduledExecutorService getScheduler() {
        return scheduler;
    }
}
