package com.capricedumardi.agent.testsupport;

import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.buffers.GenericBuffer;
import com.capricedumardi.agent.core.config.jmx.AgentDynamicConfig;
import com.capricedumardi.agent.core.model.LogEntry;
import com.capricedumardi.agent.core.model.LogRequestDto;
import com.capricedumardi.agent.core.model.MetricEntry;
import com.capricedumardi.agent.core.model.MetricRequestDto;
import com.capricedumardi.agent.core.model.SendableRequestType;

/**
 * Shared bootstrap for tests that touch the agent's process-wide singletons
 * (ConfigLoader/AgentDynamicConfig/BuffersFactory). These singletons have no
 * reset mechanism (BuffersFactory.init() is a permanent one-time effect for the
 * whole JVM, and shutdownAll() poisons it forever), so this class is the single
 * gateway every test goes through: the first caller wins the real initialization,
 * everyone else just reuses it. Never call BuffersFactory.shutdownAll() from a
 * test — it would break every other test class sharing this JVM fork.
 */
public final class AgentTestSupport {

    private static final RecordingSenderService BOOTSTRAP_SENDER = new RecordingSenderService();
    private static volatile boolean bootstrapped = false;

    private AgentTestSupport() {
    }

    public static synchronized AgentDynamicConfig dynamicConfig() {
        AgentDynamicConfig config = AgentDynamicConfig.getInstance();
        // Keep the periodic background auto-flush from firing mid-test so buffer
        // assertions stay deterministic; tests trigger flushes explicitly instead.
        config.setBufferFlushIntervalSeconds(3600);
        return config;
    }

    /**
     * Ensures BuffersFactory is initialized exactly once for the whole test JVM,
     * wired to a shared RecordingSenderService. Classes that need the shared log/metric
     * buffer (appenders, DefaultMetricsCollector, AppenderBinding) go through this and
     * must reset {@link #bootstrapSender()} in @BeforeEach to avoid cross-test pollution.
     */
    public static synchronized void ensureBuffersFactoryBootstrapped() {
        if (bootstrapped) {
            return;
        }
        BuffersFactory.init(BOOTSTRAP_SENDER, "test-app-key", "test-account-key", dynamicConfig());
        bootstrapped = true;
    }

    public static RecordingSenderService bootstrapSender() {
        ensureBuffersFactoryBootstrapped();
        return BOOTSTRAP_SENDER;
    }

    /**
     * Builds a standalone GenericBuffer<LogEntry> wired to its own RecordingSenderService,
     * isolated from the shared bootstrap buffer/sender. Only needs BuffersFactory's shared
     * scheduler to exist, which ensureBuffersFactoryBootstrapped() guarantees.
     */
    public static GenericBuffer<LogEntry, LogRequestDto> newIsolatedLogBuffer(RecordingSenderService sender, String bufferName) {
        ensureBuffersFactoryBootstrapped();
        return new GenericBuffer<>(
                entries -> new LogRequestDto("appKey", "accountKey", entries, SendableRequestType.LOG),
                sender,
                "appKey",
                "accountKey",
                dynamicConfig(),
                bufferName
        );
    }

    public static GenericBuffer<MetricEntry, MetricRequestDto> newIsolatedMetricBuffer(RecordingSenderService sender, String bufferName) {
        ensureBuffersFactoryBootstrapped();
        return new GenericBuffer<>(
                entries -> new MetricRequestDto("appKey", "accountKey", entries, SendableRequestType.METRIC),
                sender,
                "appKey",
                "accountKey",
                dynamicConfig(),
                bufferName
        );
    }

    /**
     * Polls until the condition is true or the timeout elapses. Used sparingly, only for
     * the handful of assertions that genuinely depend on the shared background scheduler
     * (e.g. batch-size-triggered async flush) rather than a direct, synchronous flush() call.
     */
    public static void awaitUntil(java.util.function.BooleanSupplier condition, long timeoutMillis) {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
