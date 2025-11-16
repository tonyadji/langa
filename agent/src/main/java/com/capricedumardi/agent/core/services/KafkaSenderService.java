package com.capricedumardi.agent.core.services;

import com.google.gson.Gson;
import com.capricedumardi.agent.core.helpers.CredentialsHelper;
import com.capricedumardi.agent.core.model.LogRequestDto;
import com.capricedumardi.agent.core.model.MetricRequestDto;
import com.capricedumardi.agent.core.model.SendableRequestDto;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KafkaSenderService implements SenderService {
    // Configuration constants
    private static final int DEFAULT_TIMEOUT_MS = 30000; // 30 seconds
    private static final int PRODUCER_CLOSE_TIMEOUT_SECONDS = 10;

    // Circuit breaker configuration
    private static final int CIRCUIT_BREAKER_THRESHOLD = 5;
    private static final long CIRCUIT_BREAKER_TIMEOUT_MS = 30000; // 30 seconds

    // Send mode configuration (can be made configurable via env var)
    private static final boolean ASYNC_SEND = getBooleanProperty("LANGA_KAFKA_ASYNC", true);

    // Instance fields
    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final CredentialsHelper credentialsHelper;
    private final Gson gson;
    private final CircuitBreaker circuitBreaker;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    // Statistics
    private final AtomicLong totalSent = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);
    private final AtomicLong totalAsyncFailed = new AtomicLong(0);


    /**
     * Constructor for KafkaSenderService
     *
     * @param bootstrapServer   Kafka broker addresses (e.g., "localhost:9092")
     * @param topic             Topic name for logs/metrics
     * @param credentialsHelper Helper for generating authentication headers
     */
    public KafkaSenderService(String bootstrapServer, String topic,
        CredentialsHelper credentialsHelper) {
        this.topic = topic;
        this.credentialsHelper = credentialsHelper;
        this.gson = new Gson();
        this.circuitBreaker = new CircuitBreaker("Kafka[" + topic + "]",
                CIRCUIT_BREAKER_THRESHOLD,
                CIRCUIT_BREAKER_TIMEOUT_MS);

        // Configure producer properties
        Properties props = new Properties();

        // Basic configuration
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Reliability configuration
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
        props.put(ProducerConfig.RETRIES_CONFIG, 3); // Retry up to 3 times
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        // Performance configuration
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // Compress messages
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 16KB batches
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Wait 10ms for batching
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32MB buffer

        // Timeout configuration
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, DEFAULT_TIMEOUT_MS);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, DEFAULT_TIMEOUT_MS * 4); // 2 minutes

        // Idempotence for exactly-once semantics
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // Create producer
        this.producer = new KafkaProducer<>(props);

        System.out.println("KafkaSenderService initialized:");
        System.out.println("  Bootstrap: " + bootstrapServer);
        System.out.println("  Topic: " + topic);
        System.out.println("  Mode: " + (ASYNC_SEND ? "ASYNC" : "SYNC"));
    }

    @Override
    public boolean send(SendableRequestDto payload) {
        // Check if closed
        if (closed.get()) {
            return false;
        }

        // Check circuit breaker
        if (!circuitBreaker.allowRequest()) {
            return false; // Circuit open - don't even try
        }

        try {
            // Serialize payload
            String key = generateKey(payload);
            String json = gson.toJson(payload);

            // Create producer record
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, json);

            // Add authentication headers
            addCredentialHeaders(record.headers());

            // Send based on mode
            boolean success;
            if (ASYNC_SEND) {
                success = sendAsync(record);
            } else {
                success = sendSync(record);
            }

            // Update circuit breaker and stats
            if (success) {
                circuitBreaker.recordSuccess();
                totalSent.incrementAndGet();
            } else {
                circuitBreaker.recordFailure();
                totalFailed.incrementAndGet();
            }

            return success;

        } catch (Exception e) {
            System.err.println("KafkaSenderService: Error sending payload: " +
                    e.getClass().getSimpleName() + ": " + e.getMessage());
            circuitBreaker.recordFailure();
            totalFailed.incrementAndGet();
            return false;
        }
    }

    /**
     * Send message asynchronously with callback tracking.
     *
     * IMPORTANT: Unlike the original implementation, this method uses a CountDownLatch
     * to wait for the async callback, so we can return accurate success/failure status.
     *
     * This adds a small amount of blocking but ensures the buffer knows if send actually succeeded.
     */
    private boolean sendAsync(ProducerRecord<String, String> record) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);

        try {
            producer.send(record, (metadata, exception) -> {
                try {
                    if (exception != null) {
                        System.err.println("KafkaSenderService: Async send failed for topic=" +
                                record.topic() + ", key=" + record.key() +
                                ": " + exception.getMessage());
                        totalAsyncFailed.incrementAndGet();
                        success.set(false);
                    } else {
                        success.set(true);
                    }
                } finally {
                    latch.countDown();
                }
            });

            boolean completed = latch.await(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            if (!completed) {
                System.err.println("KafkaSenderService: Async send timeout after " +
                        DEFAULT_TIMEOUT_MS + "ms");
                return false;
            }

            return success.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("KafkaSenderService: Interrupted while waiting for async send");
            return false;
        } catch (Exception e) {
            System.err.println("KafkaSenderService: Exception during async send: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send message synchronously (blocks until ack received).
     *
     * This is slower but guarantees we know the result before returning.
     * Useful for critical data or when you need guaranteed ordering.
     */
    private boolean sendSync(ProducerRecord<String, String> record) {
        try {
            RecordMetadata metadata = producer.send(record).get(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            return true;

        } catch (TimeoutException e) {
            System.err.println("KafkaSenderService: Sync send timeout after " + DEFAULT_TIMEOUT_MS + "ms");
            return false;

        } catch (ExecutionException e) {
            System.err.println("KafkaSenderService: Sync send failed: " +
                    e.getCause().getMessage());
            return false;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("KafkaSenderService: Interrupted during sync send");
            return false;
        }
    }

    /**
     * Add credential headers to Kafka message headers.
     * Headers include: xUserAgent, xAppKey, xAccountKey, xAgentSignature, xTimestamp
     */
    private void addCredentialHeaders(Headers headers) {
        try {
            Map<String, String> credentials = credentialsHelper.getCredentials(
                    CredentialsHelper.CredentialType.KAFKA);

            if (credentials != null) {
                credentials.forEach((key, value) ->
                        headers.add(key, value.getBytes(StandardCharsets.UTF_8))
                );
            }
        } catch (Exception e) {
            System.err.println("KafkaSenderService: Error adding credential headers: " +
                    e.getMessage());
        }
    }

    /**
     * Generate a partition key for better distribution.
     * Using appKey as partition key ensures messages from same app go to same partition.
     */
    private String generateKey(SendableRequestDto payload) {
        if (payload instanceof LogRequestDto logRequest) {
            return logRequest.appKey();
        } else if (payload instanceof MetricRequestDto metricRequest) {
            return metricRequest.appKey();
        }
        return "default";
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            System.out.println("Closing KafkaSenderService...");

            try {
                System.out.println("Flushing pending messages...");
                producer.flush();

                System.out.println("Closing Kafka producer...");
                producer.close(Duration.ofSeconds(PRODUCER_CLOSE_TIMEOUT_SECONDS));

                System.out.println("KafkaSenderService closed successfully");

            } catch (Exception e) {
                System.err.println("✗ Error closing KafkaSenderService: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }
    }

    @Override
    public String getDescription() {
        return "Kafka[" + topic + ", mode=" + (ASYNC_SEND ? "ASYNC" : "SYNC") +
                ", circuit=" + circuitBreaker.getState() + "]";
    }

    /**
     * Manually flush pending messages.
     * Useful for testing or ensuring messages are sent immediately.
     */
    public void flush() {
        if (!closed.get()) {
            try {
                producer.flush();
            } catch (Exception e) {
                System.err.println("KafkaSenderService: Error flushing messages: " + e.getMessage());
            }
        }
    }

    /**
     * Get total number of successful sends.
     */
    public long getTotalSent() {
        return totalSent.get();
    }

    /**
     * Get total number of failed sends.
     */
    public long getTotalFailed() {
        return totalFailed.get();
    }

    /**
     * Get total number of async callback failures.
     */
    public long getTotalAsyncFailed() {
        return totalAsyncFailed.get();
    }

    /**
     * Get circuit breaker state.
     */
    public CircuitBreaker.State getCircuitBreakerState() {
        return circuitBreaker.getState();
    }

    /**
     * Helper to get boolean property from environment with default.
     */
    private static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = System.getenv(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
}
