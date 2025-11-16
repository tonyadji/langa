package com.capricedumardi.agent.core.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads agent configuration from multiple sources with priority:
 * 1. System properties (-Dlanga.xxx=value)
 * 2. Environment variables (LANGA_XXX=value)
 * 3. Config file (langa-agent.properties)
 * 4. Default values
 *
 * Thread-safe singleton.
 */
public class ConfigLoader {

    private static volatile AgentConfig configInstance;
    private static final Object LOCK = new Object();

    // Config file locations (checked in order)
    private static final String[] CONFIG_FILE_PATHS = {
            "langa-agent.properties"
    };

    private ConfigLoader() {
    }

    /**
     * Get singleton configuration instance.
     * Thread-safe with double-checked locking.
     */
    public static AgentConfig getConfigInstance() {
        if (configInstance == null) {
            synchronized (LOCK) {
                if (configInstance == null) {
                    configInstance = loadConfig();
                }
            }
        }
        return configInstance;
    }

    /**
     * Reload configuration (mainly for testing).
     * Use with caution - changing config at runtime may cause issues.
     */
    public static synchronized void reloadConfig() {
        configInstance = loadConfig();
        System.out.println("Configuration reloaded");
    }

    /**
     * Load configuration from all sources.
     */
    private static AgentConfig loadConfig() {
        System.out.println("Loading Langa Agent configuration...");

        Properties fileProps = loadPropertiesFromFile();

        AgentConfig.Builder builder = new AgentConfig.Builder();

        loadBufferConfig(builder, fileProps);
        loadSchedulerConfig(builder, fileProps);
        loadHttpConfig(builder, fileProps);
        loadKafkaConfig(builder, fileProps);
        loadCircuitBreakerConfig(builder, fileProps);
        loadRetryConfig(builder, fileProps);
        loadMetadataConfig(builder, fileProps);

        AgentConfig config = builder.build();

        System.out.println("Configuration loaded successfully");
        if (config.isDebugMode()) {
            System.out.println("  " + config);
        }

        return config;
    }

    // ========== Configuration Loaders ==========

    private static void loadBufferConfig(AgentConfig.Builder builder, Properties fileProps) {
        builder.batchSize(getIntProperty("langa.buffer.batch.size", fileProps, 50));
        builder.flushIntervalSeconds(getIntProperty("langa.buffer.flush.interval.seconds", fileProps, 5));
        builder.mainQueueCapacity(getIntProperty("langa.buffer.main.queue.capacity", fileProps, 10000));
        builder.retryQueueCapacity(getIntProperty("langa.buffer.retry.queue.capacity", fileProps, 5000));
    }

    private static void loadSchedulerConfig(AgentConfig.Builder builder, Properties fileProps) {
        int defaultPoolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        builder.schedulerThreadPoolSize(getIntProperty("langa.scheduler.thread.pool.size", fileProps, defaultPoolSize));
        builder.schedulerShutdownTimeoutSeconds(getIntProperty("langa.scheduler.shutdown.timeout.seconds", fileProps, 30));
    }

    private static void loadHttpConfig(AgentConfig.Builder builder, Properties fileProps) {
        builder.httpMaxConnectionsTotal(getIntProperty("langa.http.max.connections.total", fileProps, 100));
        builder.httpMaxConnectionsPerRoute(getIntProperty("langa.http.max.connections.per.route", fileProps, 20));
        builder.httpConnectTimeoutMillis(getIntProperty("langa.http.connect.timeout.millis", fileProps, 5000));
        builder.httpSocketTimeoutMillis(getIntProperty("langa.http.socket.timeout.millis", fileProps, 10000));
        builder.httpConnectionRequestTimeoutMillis(getIntProperty("langa.http.connection.request.timeout.millis", fileProps, 30000));
        builder.httpCompressionThresholdBytes(getIntProperty("langa.http.compression.threshold.bytes", fileProps, 1024));
        builder.httpMaxRetryAttempts(getIntProperty("langa.http.max.retry.attempts", fileProps, 3));
        builder.httpBaseRetryDelayMillis(getIntProperty("langa.http.base.retry.delay.millis", fileProps, 100));
        builder.httpMaxRetryDelayMillis(getIntProperty("langa.http.max.retry.delay.millis", fileProps, 5000));
    }

    private static void loadKafkaConfig(AgentConfig.Builder builder, Properties fileProps) {
        builder.kafkaRequestTimeoutMillis(getIntProperty("langa.kafka.request.timeout.millis", fileProps, 30000));
        builder.kafkaDeliveryTimeoutMillis(getIntProperty("langa.kafka.delivery.timeout.millis", fileProps, 120000));
        builder.kafkaProducerCloseTimeoutSeconds(getIntProperty("langa.kafka.producer.close.timeout.seconds", fileProps, 10));
        builder.kafkaBatchSizeBytes(getIntProperty("langa.kafka.batch.size.bytes", fileProps, 16384));
        builder.kafkaLingerMillis(getIntProperty("langa.kafka.linger.millis", fileProps, 10));
        builder.kafkaBufferMemoryBytes(getLongProperty("langa.kafka.buffer.memory.bytes", fileProps, 33554432L));
        builder.kafkaCompressionType(getStringProperty("langa.kafka.compression.type", fileProps, "snappy"));
        builder.kafkaAcks(getStringProperty("langa.kafka.acks", fileProps, "all"));
        builder.kafkaRetries(getIntProperty("langa.kafka.retries", fileProps, 3));
        builder.kafkaMaxInFlightRequests(getIntProperty("langa.kafka.max.in.flight.requests", fileProps, 5));
        builder.kafkaEnableIdempotence(getBooleanProperty("langa.kafka.enable.idempotence", fileProps, true));
        builder.kafkaAsyncSend(getBooleanProperty("langa.kafka.async.send", fileProps, true));
    }

    private static void loadCircuitBreakerConfig(AgentConfig.Builder builder, Properties fileProps) {
        builder.circuitBreakerFailureThreshold(getIntProperty("langa.circuit.breaker.failure.threshold", fileProps, 5));
        builder.circuitBreakerOpenDurationMillis(getLongProperty("langa.circuit.breaker.open.duration.millis", fileProps, 30000L));
    }

    private static void loadRetryConfig(AgentConfig.Builder builder, Properties fileProps) {
        builder.maxRetryDelaySeconds(getIntProperty("langa.retry.max.delay.seconds", fileProps, 300));
        builder.maxConsecutiveErrors(getIntProperty("langa.retry.max.consecutive.errors", fileProps, 10));
    }

    private static void loadMetadataConfig(AgentConfig.Builder builder, Properties fileProps) {
        builder.agentVersion(getStringProperty("langa.agent.version", fileProps, "1.0.0"));
        builder.debugMode(getBooleanProperty("langa.debug.mode", fileProps, false));
    }

    // ========== Property Loaders with Priority ==========

    /**
     * Get int property with priority: System Property > Env Var > File > Default
     */
    private static int getIntProperty(String key, Properties fileProps, int defaultValue) {
        String value = getPropertyValue(key, fileProps);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("Invalid integer value for " + key + ": " + value +
                        " (using default: " + defaultValue + ")");
            }
        }
        return defaultValue;
    }

    /**
     * Get long property with priority: System Property > Env Var > File > Default
     */
    private static long getLongProperty(String key, Properties fileProps, long defaultValue) {
        String value = getPropertyValue(key, fileProps);
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                System.err.println("Invalid long value for " + key + ": " + value +
                        " (using default: " + defaultValue + ")");
            }
        }
        return defaultValue;
    }

    /**
     * Get string property with priority: System Property > Env Var > File > Default
     */
    private static String getStringProperty(String key, Properties fileProps, String defaultValue) {
        String value = getPropertyValue(key, fileProps);
        return value != null ? value : defaultValue;
    }

    /**
     * Get boolean property with priority: System Property > Env Var > File > Default
     */
    private static boolean getBooleanProperty(String key, Properties fileProps, boolean defaultValue) {
        String value = getPropertyValue(key, fileProps);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    /**
     * Get property value with priority: System Property > Env Var > File
     */
    private static String getPropertyValue(String key, Properties fileProps) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }

        String envKey = key.replace('.', '_').toUpperCase();
        String envVar = System.getenv(envKey);
        if (envVar != null && !envVar.trim().isEmpty()) {
            return envVar.trim();
        }

        if (fileProps != null) {
            String fileProp = fileProps.getProperty(key);
            if (fileProp != null && !fileProp.trim().isEmpty()) {
                return fileProp.trim();
            }
        }

        return null;
    }

    /**
     * Load properties from config file (tries multiple locations).
     */
    private static Properties loadPropertiesFromFile() {
        Properties props = new Properties();

        for (String path : CONFIG_FILE_PATHS) {
            try (InputStream input = new FileInputStream(path)) {
                props.load(input);
                System.out.println("Loaded config from: " + path);
                return props;
            } catch (Exception e) {
            }
        }

        System.out.println(" No config file found (checked: langa-agent.properties)");
        System.out.println(" Using environment variables and system properties");
        return null;
    }
}