package com.langa.agent.core.buffers;

import com.langa.agent.core.services.SenderService;

import java.util.concurrent.*;

public abstract class AbstractBuffer<T> {
    protected final BlockingQueue<T> mainQueue;
    protected final BlockingQueue<T> retryQueue;
    protected final ScheduledExecutorService scheduler;
    protected final SenderService senderService;
    protected int consecutiveSendingErrors = 0;

    protected final String appKey;
    protected final String accountKey;
    protected final int batchSize;
    protected final int flushIntervalSeconds;

     AbstractBuffer(SenderService senderService, String appKey, String accountKey,
                          int batchSize, int flushIntervalSeconds) {
        this.senderService = senderService;
        this.appKey = appKey;
        this.accountKey = accountKey;
        this.batchSize = batchSize;
        this.flushIntervalSeconds = flushIntervalSeconds;

         mainQueue = new LinkedBlockingQueue<>();
         retryQueue = new LinkedBlockingQueue<>();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::flush, flushIntervalSeconds, flushIntervalSeconds, TimeUnit.SECONDS);
        scheduler.schedule(this::retryFlush, flushIntervalSeconds, TimeUnit.SECONDS);
    }

    public void add(T entry) {
        if(mainQueue.offer(entry)) {
            if (mainQueue.size() >= batchSize) {
                scheduler.submit(this::flush);
            }
        } else {
            System.err.println("Failed to add entry to buffer");
        }
    }

    protected abstract void flush();

     protected abstract void retryFlush();

    public void shutdown() {
        scheduler.shutdown();
        flush();
    }
}
