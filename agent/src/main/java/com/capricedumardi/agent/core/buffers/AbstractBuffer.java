package com.capricedumardi.agent.core.buffers;

import com.capricedumardi.agent.core.config.AgentConfig;
import com.capricedumardi.agent.core.config.ConfigLoader;
import com.capricedumardi.agent.core.model.SendableRequestDto;
import com.capricedumardi.agent.core.services.SenderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractBuffer<T> {
    private static final Logger log = LogManager.getLogger(AbstractBuffer.class);

    protected final SenderService senderService;
    protected final String appKey;
    protected final String accountKey;
    protected final int batchSize;
    protected final int flushIntervalSeconds;
    protected final String bufferName;

    protected final BlockingQueue<T> mainQueue;
    protected final BlockingQueue<T> retryQueue;
    protected final ScheduledExecutorService scheduler;

    private final Object flushLock = new Object();
    private final AtomicBoolean flushScheduled = new AtomicBoolean(false);

    protected final AtomicInteger consecutiveSendingErrors = new AtomicInteger(0);

    private final AtomicLong totalAdded = new AtomicLong(0);
    private final AtomicLong totalFlushed = new AtomicLong(0);
    private final AtomicLong totalDropped = new AtomicLong(0);
    private final AtomicLong totalRetried = new AtomicLong(0);
    private final AtomicLong totalSendFailures = new AtomicLong(0);

    private static final AgentConfig agentConfig = ConfigLoader.getConfigInstance();

     AbstractBuffer(SenderService senderService, String appKey, String accountKey,
                          int batchSize, int flushIntervalSeconds, String bufferName) {
        this.senderService = senderService;
        this.appKey = appKey;
        this.accountKey = accountKey;
        this.batchSize = batchSize;
        this.flushIntervalSeconds = flushIntervalSeconds;
        this.bufferName = bufferName;

         mainQueue = new LinkedBlockingQueue<>(getMainQueueCapacity());
         retryQueue = new LinkedBlockingQueue<>(getRetryQueueCapacity());

        scheduler = BuffersFactory.getScheduler();
        scheduler.scheduleAtFixedRate(this::flush, flushIntervalSeconds, flushIntervalSeconds, TimeUnit.SECONDS);
    }

    public void add(T entry) {
        if (BuffersFactory.isShuttingDown()) {
            totalDropped.incrementAndGet();
            return;
        }
        //why before the offer method?
        totalAdded.incrementAndGet();
        if(mainQueue.offer(entry)) {
            if (mainQueue.size() >= batchSize && flushScheduled.compareAndSet(false, true)) {
                scheduler.submit(() -> {
                    try {
                        flush();
                    } finally {
                        flushScheduled.set(false);
                    }
                });
            }
        } else {
            log.error("Failed to add entry to buffer");
            //handle overflow
        }
    }

    public void flush() {
        synchronized (flushLock) {
            flushAndCheck(mainQueue, false);
        }
    }

    public void shutdown() {
        System.out.println(bufferName + " buffer shutting down...");
        flush();

        if (!retryQueue.isEmpty()) {
            System.out.println(bufferName + " flushing retry queue...");
            retryFlush();
        }
    }

    protected void retryFlush() {
        synchronized (flushLock) {
            flushAndCheck(retryQueue, true);
        }
    }

    /**
     * Flush entries from a queue and handle send results.
     *
     * @param processingQueue Queue to flush from
     * @param isRetry Whether this is a retry operation
     */
    private void flushAndCheck(BlockingQueue<T> processingQueue, boolean isRetry) {
        if (processingQueue.isEmpty()) {
            return;
        }

        var entries = new ArrayList<T>();
        processingQueue.drainTo(entries, batchSize);

        if (entries.isEmpty()) {
            return;
        }

        try {
            var dto = mapToSendableRequest(entries);
            boolean isSendSuccess = senderService.send(dto);

            if (isSendSuccess) {
                totalFlushed.addAndGet(entries.size());
                consecutiveSendingErrors.set(0);

                if (isRetry) {
                    System.out.println(bufferName + " retry flush succeeded (" +
                            entries.size() + " entries)");
                }
            } else {
                handleSendFailure(entries, isRetry);
            }

        } catch (Exception e) {
            System.err.println(bufferName + " flush error: " + e.getMessage());
            handleSendFailure(entries, isRetry);
        }
    }

    private void handleSendFailure(java.util.ArrayList<T> entries, boolean isRetry) {
        totalSendFailures.incrementAndGet();
        int errors = consecutiveSendingErrors.incrementAndGet();

        if (isRetry) {
            totalDropped.addAndGet(entries.size());
            System.err.println(bufferName + " retry failed, dropping " +
                    entries.size() + " entries (consecutive errors: " + errors + ")");
        } else {
            int moved = 0;
            int dropped = 0;

            for (T entry : entries) {
                if (retryQueue.offer(entry)) {
                    moved++;
                } else {
                    dropped++;
                    totalDropped.incrementAndGet();
                }
            }

            totalRetried.addAndGet(moved);

            if (dropped > 0) {
                System.err.println( bufferName + " retry queue full: dropped " +
                        dropped + " of " + entries.size() + " entries");
            }

            scheduleRetryFlush();
        }
    }

    private void scheduleRetryFlush() {
        int cappedErrors = Math.min(consecutiveSendingErrors.get(), agentConfig.getMaxConsecutiveErrors());

        int baseDelay = (int) Math.pow(2, cappedErrors);

        int jitter = ThreadLocalRandom.current().nextInt(0, baseDelay / 2 + 1);

        int retryDelay = Math.min(baseDelay + jitter, agentConfig.getMaxRetryDelaySeconds());

        System.out.println(bufferName + " scheduling retry in " + retryDelay +
                " seconds (consecutive errors: " + consecutiveSendingErrors.get() + ")");

        scheduler.schedule(this::retryFlush, retryDelay, TimeUnit.SECONDS);
    }

    public BufferStats getStats() {
        return new BufferStats(
                bufferName,
                totalAdded.get(),
                totalFlushed.get(),
                totalDropped.get(),
                totalRetried.get(),
                totalSendFailures.get(),
                mainQueue.size(),
                retryQueue.size(),
                consecutiveSendingErrors.get(),
                getMainQueueCapacity(),
                getRetryQueueCapacity()
        );
    }

    protected int getMainQueueCapacity() {
        String envVar = System.getenv("LANGA_MAIN_QUEUE_CAPACITY");
        if (envVar != null) {
            try {
                return Integer.parseInt(envVar);
            } catch (NumberFormatException e) {
                System.err.println("Invalid LANGA_MAIN_QUEUE_CAPACITY: " + envVar);
            }
        }
        return agentConfig.getMainQueueCapacity();
    }

    protected int getRetryQueueCapacity() {
        String envVar = System.getenv("LANGA_RETRY_QUEUE_CAPACITY");
        if (envVar != null) {
            try {
                return Integer.parseInt(envVar);
            } catch (NumberFormatException e) {
                System.err.println("Invalid LANGA_RETRY_QUEUE_CAPACITY: " + envVar);
            }
        }
        return agentConfig.getRetryQueueCapacity();
    }

    protected abstract SendableRequestDto mapToSendableRequest(java.util.List<T> entries);
}
