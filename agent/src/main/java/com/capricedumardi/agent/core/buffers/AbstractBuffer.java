package com.capricedumardi.agent.core.buffers;

import com.capricedumardi.agent.core.services.SenderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

public abstract class AbstractBuffer<T> {
    private static final Logger log = LogManager.getLogger(AbstractBuffer.class);
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

        scheduler = BuffersFactory.getScheduler();
        scheduler.scheduleAtFixedRate(this::flush, flushIntervalSeconds, flushIntervalSeconds, TimeUnit.SECONDS);
    }

    public void add(T entry) {
        if(mainQueue.offer(entry)) {
            if (mainQueue.size() >= batchSize) {
                scheduler.submit(this::flush);
            }
        } else {
            log.error("Failed to add entry to buffer");
        }
    }

    protected abstract void flush();

     protected abstract void retryFlush();

    public void shutdown() {
        flush();
    }
}
