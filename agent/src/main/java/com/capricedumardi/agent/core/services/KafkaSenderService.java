package com.capricedumardi.agent.core.services;

import com.capricedumardi.agent.core.config.AgentConfig;
import com.capricedumardi.agent.core.config.ConfigLoader;
import com.capricedumardi.agent.core.config.LangaPrinter;
import com.capricedumardi.agent.core.helpers.CredentialsHelper;
import com.capricedumardi.agent.core.model.LogRequestDto;
import com.capricedumardi.agent.core.model.MetricRequestDto;
import com.capricedumardi.agent.core.model.SendableRequestDto;
import com.google.gson.Gson;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringSerializer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class KafkaSenderService implements SenderService {

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

    private static final AgentConfig agentConfig = ConfigLoader.getConfigInstance();
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
                agentConfig.getCircuitBreakerFailureThreshold(),
                agentConfig.getCircuitBreakerOpenDurationMillis());

        Properties props = new Properties();

        // Basic configuration
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Reliability configuration
        props.put(ProducerConfig.ACKS_CONFIG, agentConfig.getKafkaAcks()); // Wait for all replicas
        props.put(ProducerConfig.RETRIES_CONFIG, agentConfig.getKafkaRetries()); // Retry up to 3 times
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, agentConfig.getKafkaMaxInFlightRequests());

        // Performance configuration
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, agentConfig.getKafkaCompressionType()); // Compress messages
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, agentConfig.getKafkaBatchSizeBytes()); // 16KB batches
        props.put(ProducerConfig.LINGER_MS_CONFIG, agentConfig.getKafkaLingerMillis()); // Wait 10ms for batching
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, agentConfig.getKafkaBufferMemoryBytes()); // 32MB buffer

        // Timeout configuration
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, agentConfig.getKafkaRequestTimeoutMillis());
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, agentConfig.getKafkaRequestTimeoutMillis() * 4); // 2 minutes

        // Idempotence for exactly-once semantics
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, agentConfig.isKafkaEnableIdempotence());

        // Create producer
        this.producer = new KafkaProducer<>(props);

        LangaPrinter.printTrace("KafkaSenderService initialized:");
        LangaPrinter.printTrace("Bootstrap: " + bootstrapServer);
        LangaPrinter.printTrace("Topic: " + topic);
        LangaPrinter.printTrace("Mode: " + (agentConfig.isKafkaAsyncSend() ? "ASYNC" : "SYNC"));
    }

    @Override
    public boolean send(SendableRequestDto payload) {
        if (closed.get()) {
            return false;
        }

        if (!circuitBreaker.allowRequest()) {
            return false;
        }

        try {
            String key = generateKey(payload);
            String json = gson.toJson(payload);

            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, json);

            addCredentialHeaders(producerRecord.headers());

            boolean success;
            if (ASYNC_SEND) {
                success = sendAsync(producerRecord);
            } else {
                success = sendSync(producerRecord);
            }

            if (success) {
                circuitBreaker.recordSuccess();
                totalSent.incrementAndGet();
            } else {
                circuitBreaker.recordFailure();
                totalFailed.incrementAndGet();
            }

            return success;

        } catch (Exception e) {
            LangaPrinter.printError("KafkaSenderService: Error sending payload: " +
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
    private boolean sendAsync(ProducerRecord<String, String> producerRecord) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);

        try {
            producer.send(producerRecord, (metadata, exception) -> {
                try {
                    if (exception != null) {
                        LangaPrinter.printError("KafkaSenderService: Async send failed for topic=" +
                                producerRecord.topic() + ", key=" + producerRecord.key() +
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

            boolean completed = latch.await(agentConfig.getKafkaRequestTimeoutMillis(), TimeUnit.MILLISECONDS);

            if (!completed) {
                LangaPrinter.printError("KafkaSenderService: Async send timeout after " +
                        agentConfig.getKafkaRequestTimeoutMillis() + "ms");
                return false;
            }

            return success.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LangaPrinter.printError("KafkaSenderService: Interrupted while waiting for async send");
            return false;
        } catch (Exception e) {
            LangaPrinter.printError("KafkaSenderService: Exception during async send: " + e.getMessage());
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
            RecordMetadata metadata = producer.send(record).get(agentConfig.getKafkaRequestTimeoutMillis(), TimeUnit.MILLISECONDS);
            return true;

        } catch (TimeoutException e) {
            LangaPrinter.printError("KafkaSenderService: Sync send timeout after " + agentConfig.getKafkaRequestTimeoutMillis() + "ms");
            return false;

        } catch (ExecutionException e) {
            LangaPrinter.printError("KafkaSenderService: Sync send failed: " +
                    e.getCause().getMessage());
            return false;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LangaPrinter.printError("KafkaSenderService: Interrupted during sync send");
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
            LangaPrinter.printError("KafkaSenderService: Error adding credential headers: " +
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
            LangaPrinter.printTrace("Closing KafkaSenderService...");

            try {
                LangaPrinter.printTrace("Flushing pending messages...");
                producer.flush();

                LangaPrinter.printTrace("Closing Kafka producer...");
                producer.close(Duration.ofSeconds(agentConfig.getKafkaProducerCloseTimeoutSeconds()));

                LangaPrinter.printTrace("KafkaSenderService closed successfully");

            } catch (Exception e) {
                LangaPrinter.printError("✗ Error closing KafkaSenderService: " + e.getMessage());
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
                LangaPrinter.printError("KafkaSenderService: Error flushing messages: " + e.getMessage());
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
        return Optional.ofNullable(agentConfig).map(AgentConfig::isKafkaAsyncSend).orElse(defaultValue);
    }
}
