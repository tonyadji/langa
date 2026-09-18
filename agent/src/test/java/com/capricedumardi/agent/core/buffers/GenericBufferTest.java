package com.capricedumardi.agent.core.buffers;

import com.capricedumardi.agent.core.model.LogEntry;
import com.capricedumardi.agent.testsupport.AgentTestSupport;
import com.capricedumardi.agent.testsupport.RecordingSenderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericBufferTest {

    private final RecordingSenderService sender = new RecordingSenderService();

    @AfterEach
    void resetSharedDynamicConfig() {
        AgentTestSupport.dynamicConfig().setBufferBatchSize(50);
    }

    private GenericBuffer<LogEntry, ?> newBuffer(String name) {
        return AgentTestSupport.newIsolatedLogBuffer(sender, name);
    }

    private LogEntry entry(String message) {
        return new LogEntry(message, "INFO", "logger", "2024-01-01T00:00:00Z");
    }

    @Test
    void addingBelowBatchSizeDoesNotAutoFlush() {
        var buffer = newBuffer("below-batch");

        buffer.add(entry("one"));

        assertEquals(0, sender.sendCallCount());
        assertEquals(1, buffer.getStats().getTotalAdded());
    }

    @Test
    void manualFlushSendsAllQueuedEntries() {
        var buffer = newBuffer("manual-flush");

        buffer.add(entry("one"));
        buffer.add(entry("two"));
        buffer.flush();

        assertEquals(1, sender.sendCallCount());
        assertEquals(1, sender.sentPayloads().size());
        assertEquals(2, buffer.getStats().getTotalFlushed());
        assertEquals(0, buffer.getStats().getMainQueueSize());
    }

    @Test
    void flushOnEmptyQueueIsANoOp() {
        var buffer = newBuffer("empty-flush");

        buffer.flush();

        assertEquals(0, sender.sendCallCount());
    }

    @Test
    void failedFlushMovesEntriesToRetryQueue() {
        var buffer = newBuffer("failed-flush");
        sender.failNext();

        buffer.add(entry("one"));
        buffer.flush();

        BufferStats stats = buffer.getStats();
        assertEquals(0, stats.getTotalFlushed());
        assertEquals(1, stats.getTotalSendFailures());
        assertEquals(1, stats.getRetryQueueSize());
        assertEquals(1, stats.getConsecutiveErrors());
    }

    @Test
    void shutdownFlushesBothMainAndRetryQueues() {
        var buffer = newBuffer("shutdown-drains-retry");

        sender.failNext();
        buffer.add(entry("one"));
        buffer.flush();
        assertEquals(1, buffer.getStats().getRetryQueueSize(), "entry should have moved to the retry queue");

        sender.succeedNext();
        buffer.shutdown();

        BufferStats stats = buffer.getStats();
        assertEquals(0, stats.getRetryQueueSize(), "shutdown must drain the retry queue, not just the main queue");
        assertEquals(1, stats.getTotalFlushed());
        assertEquals(1, sender.sentPayloads().size());
    }

    @Test
    void repeatedRetryFailureIncreasesConsecutiveErrorCount() {
        var buffer = newBuffer("repeated-failures");
        sender.failNext();

        buffer.add(entry("one"));
        buffer.flush();
        assertEquals(1, buffer.getStats().getConsecutiveErrors());

        sender.failNext();
        buffer.retryFlush();

        assertEquals(0, buffer.getStats().getRetryQueueSize(), "a failed retry drops the entries instead of re-queueing them");
        assertEquals(1, buffer.getStats().getTotalDropped());
    }

    @Test
    void reachingBatchSizeTriggersAnAsynchronousFlush() {
        AgentTestSupport.dynamicConfig().setBufferBatchSize(2);
        var buffer = newBuffer("batch-triggered");

        buffer.add(entry("one"));
        buffer.add(entry("two"));

        AgentTestSupport.awaitUntil(() -> sender.sendCallCount() > 0, 2000);

        assertTrue(sender.sendCallCount() > 0);
    }

    @Test
    void printStatsDoesNotThrow() {
        var buffer = newBuffer("print-stats");
        buffer.add(entry("one"));

        buffer.printStats();
    }

    @Test
    void statsReflectMultipleAddsAndOneFlush() {
        var buffer = newBuffer("stats-check");

        buffer.add(entry("one"));
        buffer.add(entry("two"));
        buffer.add(entry("three"));
        buffer.flush();

        BufferStats stats = buffer.getStats();
        assertEquals(3, stats.getTotalAdded());
        assertEquals(3, stats.getTotalFlushed());
        assertEquals("stats-check", stats.getBufferName());
        assertTrue(stats.isHealthy());
    }
}
