package com.capricedumardi.agent.core.buffers;

import com.capricedumardi.agent.core.model.LogEntry;
import com.capricedumardi.agent.testsupport.AgentTestSupport;
import com.capricedumardi.agent.testsupport.RecordingSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BuffersFactory.init()/shutdownAll() have permanent, JVM-wide effects with no reset
 * hook, so every test here relies on the ONE shared bootstrap from AgentTestSupport
 * (never calls init() with its own sender, never calls shutdownAll()).
 */
class BuffersFactoryTest {

    @BeforeEach
    void ensureBootstrapped() {
        AgentTestSupport.ensureBuffersFactoryBootstrapped();
        AgentTestSupport.bootstrapSender().reset();
    }

    @Test
    void isInitializedIsTrueAfterBootstrap() {
        assertTrue(BuffersFactory.isInitialized());
    }

    @Test
    void getLogAndMetricBufferInstancesReturnNonNullBuffers() {
        assertNotNull(BuffersFactory.getLogBufferInstance());
        assertNotNull(BuffersFactory.getMetricBufferInstance());
    }

    @Test
    void initIsIdempotentAndKeepsTheSameBufferInstances() {
        var logBufferBefore = BuffersFactory.getLogBufferInstance();
        var metricBufferBefore = BuffersFactory.getMetricBufferInstance();

        // A second init() call (even with a different sender) must be a no-op: the
        // agent is only ever wired to the first sender it was initialized with.
        BuffersFactory.init(new RecordingSenderService(), "other-app", "other-account", AgentTestSupport.dynamicConfig());

        assertSame(logBufferBefore, BuffersFactory.getLogBufferInstance());
        assertSame(metricBufferBefore, BuffersFactory.getMetricBufferInstance());
    }

    @Test
    void getSchedulerReturnsANonNullSharedScheduler() {
        assertNotNull(BuffersFactory.getScheduler());
    }

    @Test
    void bufferStatsAreExposedAfterActivity() {
        // The shared bootstrap log buffer is also used by other test classes (appender
        // tests, DefaultMetricsCollectorTest, ...), so totalFlushed accumulates across
        // the whole suite: assert the delta, not an absolute value.
        RecordingSenderService sender = AgentTestSupport.bootstrapSender();
        long before = BuffersFactory.getLogBufferStats().getTotalFlushed();

        BuffersFactory.getLogBufferInstance().add(new LogEntry("m", "INFO", "l", "ts"));
        BuffersFactory.getLogBufferInstance().flush();

        BufferStats stats = BuffersFactory.getLogBufferStats();

        assertNotNull(stats);
        assertEquals(before + 1, stats.getTotalFlushed());
        assertEquals(1, sender.sentPayloads().size());
    }

    @Test
    void isShuttingDownIsFalseDuringNormalOperation() {
        assertTrue(BuffersFactory.isInitialized());
        assertEquals(false, BuffersFactory.isShuttingDown());
    }
}
