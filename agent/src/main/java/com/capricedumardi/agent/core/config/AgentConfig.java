package com.capricedumardi.agent.core.config;

/**
 * Central configuration for Langa Agent.
 *
 * Contains all tunable parameters for buffers, senders, circuit breakers, etc.
 * Immutable once loaded - all fields are final.
 */
public class AgentConfig {
    // ========== Buffer Configuration ==========
    private final String ingestionUrl;
    private final String secret;

    // ========== Buffer Configuration ==========

    /** Number of entries to batch before sending (both log and metric buffers) */
    private final int batchSize;

    /** Seconds between automatic flushes (both log and metric buffers) */
    private final int flushIntervalSeconds;

    /** Main queue capacity (prevents OOM) */
    private final int mainQueueCapacity;

    /** Retry queue capacity */
    private final int retryQueueCapacity;

    // ========== Scheduler Configuration ==========

    /** Number of scheduler threads (default: max(2, CPU cores / 2)) */
    private final int schedulerThreadPoolSize;

    /** Scheduler shutdown timeout in seconds */
    private final int schedulerShutdownTimeoutSeconds;

    // ========== HTTP Configuration ==========

    /** Maximum total HTTP connections */
    private final int httpMaxConnectionsTotal;

    /** Maximum HTTP connections per route */
    private final int httpMaxConnectionsPerRoute;

    /** HTTP connect timeout in milliseconds */
    private final int httpConnectTimeoutMillis;

    /** HTTP socket (read) timeout in milliseconds */
    private final int httpSocketTimeoutMillis;

    /** HTTP connection request timeout in milliseconds */
    private final int httpConnectionRequestTimeoutMillis;

    /** Wether or not the HTTP request should be compressed */
    private final boolean httpCompressionEnabled;

    /** Threshold in bytes for HTTP compression (compress if payload > this) */
    private final int httpCompressionThresholdBytes;

    /** Maximum HTTP retry attempts */
    private final int httpMaxRetryAttempts;

    /** Base HTTP retry delay in milliseconds (for exponential backoff) */
    private final int httpBaseRetryDelayMillis;

    /** Maximum HTTP retry delay in milliseconds */
    private final int httpMaxRetryDelayMillis;

    // ========== Kafka Configuration ==========

    /** Kafka request timeout in milliseconds */
    private final int kafkaRequestTimeoutMillis;

    /** Kafka delivery timeout in milliseconds */
    private final int kafkaDeliveryTimeoutMillis;

    /** Kafka producer close timeout in seconds */
    private final int kafkaProducerCloseTimeoutSeconds;

    /** Kafka batch size in bytes */
    private final int kafkaBatchSizeBytes;

    /** Kafka linger time in milliseconds (wait for batching) */
    private final int kafkaLingerMillis;

    /** Kafka buffer memory in bytes */
    private final long kafkaBufferMemoryBytes;

    /** Kafka compression type (snappy, gzip, lz4, zstd, none) */
    private final String kafkaCompressionType;

    /** Kafka acks configuration (all, 1, 0) */
    private final String kafkaAcks;

    /** Kafka retries */
    private final int kafkaRetries;

    /** Kafka max in-flight requests per connection */
    private final int kafkaMaxInFlightRequests;

    /** Enable Kafka idempotence (exactly-once) */
    private final boolean kafkaEnableIdempotence;

    /** Kafka async send mode (true = async with tracking, false = sync) */
    private final boolean kafkaAsyncSend;

    // ========== Circuit Breaker Configuration ==========

    /** Number of consecutive failures before opening circuit */
    private final int circuitBreakerFailureThreshold;

    /** How long to wait before testing recovery (milliseconds) */
    private final long circuitBreakerOpenDurationMillis;

    // ========== Retry Configuration (Generic) ==========

    /** Maximum retry delay for exponential backoff (seconds) */
    private final int maxRetryDelaySeconds;

    /** Maximum consecutive errors before stopping retry */
    private final int maxConsecutiveErrors;

    // ========== Agent Metadata ==========

    /** Agent version for User-Agent header */
    private final String agentVersion;

    /** Enable debug mode (more verbose output) */
    private final boolean debugMode;

    // ========== Constructor ==========

    /**
     * Package-private constructor - use ConfigLoader.getConfigInstance()
     */
    AgentConfig(Builder builder) {
        // Backend
        this.ingestionUrl = builder.ingestionUrl;
        this.secret = builder.secret;

        // Buffer
        this.batchSize = builder.batchSize;
        this.flushIntervalSeconds = builder.flushIntervalSeconds;
        this.mainQueueCapacity = builder.mainQueueCapacity;
        this.retryQueueCapacity = builder.retryQueueCapacity;

        // Scheduler
        this.schedulerThreadPoolSize = builder.schedulerThreadPoolSize;
        this.schedulerShutdownTimeoutSeconds = builder.schedulerShutdownTimeoutSeconds;

        // HTTP
        this.httpMaxConnectionsTotal = builder.httpMaxConnectionsTotal;
        this.httpMaxConnectionsPerRoute = builder.httpMaxConnectionsPerRoute;
        this.httpConnectTimeoutMillis = builder.httpConnectTimeoutMillis;
        this.httpSocketTimeoutMillis = builder.httpSocketTimeoutMillis;
        this.httpConnectionRequestTimeoutMillis = builder.httpConnectionRequestTimeoutMillis;
        this.httpCompressionEnabled = builder.httpCompressionEnabled;
        this.httpCompressionThresholdBytes = builder.httpCompressionThresholdBytes;
        this.httpMaxRetryAttempts = builder.httpMaxRetryAttempts;
        this.httpBaseRetryDelayMillis = builder.httpBaseRetryDelayMillis;
        this.httpMaxRetryDelayMillis = builder.httpMaxRetryDelayMillis;

        // Kafka
        this.kafkaRequestTimeoutMillis = builder.kafkaRequestTimeoutMillis;
        this.kafkaDeliveryTimeoutMillis = builder.kafkaDeliveryTimeoutMillis;
        this.kafkaProducerCloseTimeoutSeconds = builder.kafkaProducerCloseTimeoutSeconds;
        this.kafkaBatchSizeBytes = builder.kafkaBatchSizeBytes;
        this.kafkaLingerMillis = builder.kafkaLingerMillis;
        this.kafkaBufferMemoryBytes = builder.kafkaBufferMemoryBytes;
        this.kafkaCompressionType = builder.kafkaCompressionType;
        this.kafkaAcks = builder.kafkaAcks;
        this.kafkaRetries = builder.kafkaRetries;
        this.kafkaMaxInFlightRequests = builder.kafkaMaxInFlightRequests;
        this.kafkaEnableIdempotence = builder.kafkaEnableIdempotence;
        this.kafkaAsyncSend = builder.kafkaAsyncSend;

        // Circuit Breaker
        this.circuitBreakerFailureThreshold = builder.circuitBreakerFailureThreshold;
        this.circuitBreakerOpenDurationMillis = builder.circuitBreakerOpenDurationMillis;

        // Retry
        this.maxRetryDelaySeconds = builder.maxRetryDelaySeconds;
        this.maxConsecutiveErrors = builder.maxConsecutiveErrors;

        // Metadata
        this.agentVersion = builder.agentVersion;
        this.debugMode = builder.debugMode;
    }

    // ========== Getters ==========

    // Backend
    public String getIngestionUrl() { return ingestionUrl; }
    public String getSecret() { return secret; }

    // Buffer
    public int getBatchSize() { return batchSize; }
    public int getFlushIntervalSeconds() { return flushIntervalSeconds; }
    public int getMainQueueCapacity() { return mainQueueCapacity; }
    public int getRetryQueueCapacity() { return retryQueueCapacity; }

    // Scheduler
    public int getSchedulerThreadPoolSize() { return schedulerThreadPoolSize; }
    public int getSchedulerShutdownTimeoutSeconds() { return schedulerShutdownTimeoutSeconds; }

    // HTTP
    public int getHttpMaxConnectionsTotal() { return httpMaxConnectionsTotal; }
    public int getHttpMaxConnectionsPerRoute() { return httpMaxConnectionsPerRoute; }
    public int getHttpConnectTimeoutMillis() { return httpConnectTimeoutMillis; }
    public int getHttpSocketTimeoutMillis() { return httpSocketTimeoutMillis; }
    public int getHttpConnectionRequestTimeoutMillis() { return httpConnectionRequestTimeoutMillis; }
    public boolean isHttpCompressionEnabled() { return httpCompressionEnabled; }
    public int getHttpCompressionThresholdBytes() { return httpCompressionThresholdBytes; }
    public int getHttpMaxRetryAttempts() { return httpMaxRetryAttempts; }
    public int getHttpBaseRetryDelayMillis() { return httpBaseRetryDelayMillis; }
    public int getHttpMaxRetryDelayMillis() { return httpMaxRetryDelayMillis; }

    // Kafka
    public int getKafkaRequestTimeoutMillis() { return kafkaRequestTimeoutMillis; }
    public int getKafkaDeliveryTimeoutMillis() { return kafkaDeliveryTimeoutMillis; }
    public int getKafkaProducerCloseTimeoutSeconds() { return kafkaProducerCloseTimeoutSeconds; }
    public int getKafkaBatchSizeBytes() { return kafkaBatchSizeBytes; }
    public int getKafkaLingerMillis() { return kafkaLingerMillis; }
    public long getKafkaBufferMemoryBytes() { return kafkaBufferMemoryBytes; }
    public String getKafkaCompressionType() { return kafkaCompressionType; }
    public String getKafkaAcks() { return kafkaAcks; }
    public int getKafkaRetries() { return kafkaRetries; }
    public int getKafkaMaxInFlightRequests() { return kafkaMaxInFlightRequests; }
    public boolean isKafkaEnableIdempotence() { return kafkaEnableIdempotence; }
    public boolean isKafkaAsyncSend() { return kafkaAsyncSend; }

    // Circuit Breaker
    public int getCircuitBreakerFailureThreshold() { return circuitBreakerFailureThreshold; }
    public long getCircuitBreakerOpenDurationMillis() { return circuitBreakerOpenDurationMillis; }

    // Retry
    public int getMaxRetryDelaySeconds() { return maxRetryDelaySeconds; }
    public int getMaxConsecutiveErrors() { return maxConsecutiveErrors; }

    // Metadata
    public String getAgentVersion() { return agentVersion; }
    public boolean isDebugMode() { return debugMode; }

    // ========== Builder ==========

    /**
     * Builder for AgentConfig with default values.
     */
    public static class Builder {
        // Backend defaults
        private String ingestionUrl = "https://api.langa.capricedumardi.com/ingest";
        private String secret = "";

        // Buffer defaults
        private int batchSize = 50;
        private int flushIntervalSeconds = 5;
        private int mainQueueCapacity = 10000;
        private int retryQueueCapacity = 5000;

        // Scheduler defaults
        private int schedulerThreadPoolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        private int schedulerShutdownTimeoutSeconds = 30;

        // HTTP defaults
        private int httpMaxConnectionsTotal = 100;
        private int httpMaxConnectionsPerRoute = 20;
        private int httpConnectTimeoutMillis = 5000;
        private int httpSocketTimeoutMillis = 10000;
        private int httpConnectionRequestTimeoutMillis = 30000;
        private boolean httpCompressionEnabled = false;
        private int httpCompressionThresholdBytes = 1024;
        private int httpMaxRetryAttempts = 3;
        private int httpBaseRetryDelayMillis = 100;
        private int httpMaxRetryDelayMillis = 5000;

        // Kafka defaults
        private int kafkaRequestTimeoutMillis = 30000;
        private int kafkaDeliveryTimeoutMillis = 120000;
        private int kafkaProducerCloseTimeoutSeconds = 10;
        private int kafkaBatchSizeBytes = 16384;
        private int kafkaLingerMillis = 10;
        private long kafkaBufferMemoryBytes = 33554432L; // 32MB
        private String kafkaCompressionType = "snappy";
        private String kafkaAcks = "all";
        private int kafkaRetries = 3;
        private int kafkaMaxInFlightRequests = 5;
        private boolean kafkaEnableIdempotence = true;
        private boolean kafkaAsyncSend = true;

        // Circuit Breaker defaults
        private int circuitBreakerFailureThreshold = 5;
        private long circuitBreakerOpenDurationMillis = 30000;

        // Retry defaults
        private int maxRetryDelaySeconds = 300; // 5 minutes
        private int maxConsecutiveErrors = 10;

        // Metadata defaults
        private String agentVersion = "langa-agent-v1.0.0";
        private boolean debugMode = false;

        // Buffer
        public Builder batchSize(int batchSize) { this.batchSize = batchSize; return this; }
        public Builder flushIntervalSeconds(int flushIntervalSeconds) { this.flushIntervalSeconds = flushIntervalSeconds; return this; }
        public Builder mainQueueCapacity(int mainQueueCapacity) { this.mainQueueCapacity = mainQueueCapacity; return this; }
        public Builder retryQueueCapacity(int retryQueueCapacity) { this.retryQueueCapacity = retryQueueCapacity; return this; }

        // Scheduler
        public Builder schedulerThreadPoolSize(int schedulerThreadPoolSize) { this.schedulerThreadPoolSize = schedulerThreadPoolSize; return this; }
        public Builder schedulerShutdownTimeoutSeconds(int schedulerShutdownTimeoutSeconds) { this.schedulerShutdownTimeoutSeconds = schedulerShutdownTimeoutSeconds; return this; }

        // HTTP
        public Builder httpMaxConnectionsTotal(int httpMaxConnectionsTotal) { this.httpMaxConnectionsTotal = httpMaxConnectionsTotal; return this; }
        public Builder httpMaxConnectionsPerRoute(int httpMaxConnectionsPerRoute) { this.httpMaxConnectionsPerRoute = httpMaxConnectionsPerRoute; return this; }
        public Builder httpConnectTimeoutMillis(int httpConnectTimeoutMillis) { this.httpConnectTimeoutMillis = httpConnectTimeoutMillis; return this; }
        public Builder httpSocketTimeoutMillis(int httpSocketTimeoutMillis) { this.httpSocketTimeoutMillis = httpSocketTimeoutMillis; return this; }
        public Builder httpConnectionRequestTimeoutMillis(int httpConnectionRequestTimeoutMillis) { this.httpConnectionRequestTimeoutMillis = httpConnectionRequestTimeoutMillis; return this; }
        public Builder httpCompressionEnabled(boolean httpCompressionEnabled) { this.httpCompressionEnabled = httpCompressionEnabled; return this; }
        public Builder httpCompressionThresholdBytes(int httpCompressionThresholdBytes) { this.httpCompressionThresholdBytes = httpCompressionThresholdBytes; return this; }
        public Builder httpMaxRetryAttempts(int httpMaxRetryAttempts) { this.httpMaxRetryAttempts = httpMaxRetryAttempts; return this; }
        public Builder httpBaseRetryDelayMillis(int httpBaseRetryDelayMillis) { this.httpBaseRetryDelayMillis = httpBaseRetryDelayMillis; return this; }
        public Builder httpMaxRetryDelayMillis(int httpMaxRetryDelayMillis) { this.httpMaxRetryDelayMillis = httpMaxRetryDelayMillis; return this; }

        // Kafka
        public Builder kafkaRequestTimeoutMillis(int kafkaRequestTimeoutMillis) { this.kafkaRequestTimeoutMillis = kafkaRequestTimeoutMillis; return this; }
        public Builder kafkaDeliveryTimeoutMillis(int kafkaDeliveryTimeoutMillis) { this.kafkaDeliveryTimeoutMillis = kafkaDeliveryTimeoutMillis; return this; }
        public Builder kafkaProducerCloseTimeoutSeconds(int kafkaProducerCloseTimeoutSeconds) { this.kafkaProducerCloseTimeoutSeconds = kafkaProducerCloseTimeoutSeconds; return this; }
        public Builder kafkaBatchSizeBytes(int kafkaBatchSizeBytes) { this.kafkaBatchSizeBytes = kafkaBatchSizeBytes; return this; }
        public Builder kafkaLingerMillis(int kafkaLingerMillis) { this.kafkaLingerMillis = kafkaLingerMillis; return this; }
        public Builder kafkaBufferMemoryBytes(long kafkaBufferMemoryBytes) { this.kafkaBufferMemoryBytes = kafkaBufferMemoryBytes; return this; }
        public Builder kafkaCompressionType(String kafkaCompressionType) { this.kafkaCompressionType = kafkaCompressionType; return this; }
        public Builder kafkaAcks(String kafkaAcks) { this.kafkaAcks = kafkaAcks; return this; }
        public Builder kafkaRetries(int kafkaRetries) { this.kafkaRetries = kafkaRetries; return this; }
        public Builder kafkaMaxInFlightRequests(int kafkaMaxInFlightRequests) { this.kafkaMaxInFlightRequests = kafkaMaxInFlightRequests; return this; }
        public Builder kafkaEnableIdempotence(boolean kafkaEnableIdempotence) { this.kafkaEnableIdempotence = kafkaEnableIdempotence; return this; }
        public Builder kafkaAsyncSend(boolean kafkaAsyncSend) { this.kafkaAsyncSend = kafkaAsyncSend; return this; }

        // Circuit Breaker
        public Builder circuitBreakerFailureThreshold(int circuitBreakerFailureThreshold) { this.circuitBreakerFailureThreshold = circuitBreakerFailureThreshold; return this; }
        public Builder circuitBreakerOpenDurationMillis(long circuitBreakerOpenDurationMillis) { this.circuitBreakerOpenDurationMillis = circuitBreakerOpenDurationMillis; return this; }

        // Retry
        public Builder maxRetryDelaySeconds(int maxRetryDelaySeconds) { this.maxRetryDelaySeconds = maxRetryDelaySeconds; return this; }
        public Builder maxConsecutiveErrors(int maxConsecutiveErrors) { this.maxConsecutiveErrors = maxConsecutiveErrors; return this; }

        // Metadata
        public Builder agentVersion(String agentVersion) { this.agentVersion = agentVersion; return this; }
        public Builder debugMode(boolean debugMode) { this.debugMode = debugMode; return this; }

        public AgentConfig build() {
            return new AgentConfig(this);
        }
    }

    @Override
    public String toString() {
        return "AgentConfig{" +
                "batchSize=" + batchSize +
                ", flushIntervalSeconds=" + flushIntervalSeconds +
                ", mainQueueCapacity=" + mainQueueCapacity +
                ", retryQueueCapacity=" + retryQueueCapacity +
                ", circuitBreakerFailureThreshold=" + circuitBreakerFailureThreshold +
                ", agentVersion='" + agentVersion + '\'' +
                ", debugMode=" + debugMode +
                '}';
    }
}