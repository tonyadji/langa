package com.langa.agent.core.services;

import com.google.gson.Gson;
import com.langa.agent.core.helpers.CredentialsHelper;
import com.langa.agent.core.model.LogRequestDto;
import com.langa.agent.core.model.MetricRequestDto;
import com.langa.agent.core.model.SendableRequestDto;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KafkaSenderService implements SenderService {
    private static final Logger log = LogManager.getLogger(KafkaSenderService.class);
    private static final Gson gson = new Gson();

    private final KafkaProducer<String, String> producer;
    private final String topic;
    private final CredentialsHelper credentialsHelper;


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

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Performance et reliability settings
        props.put(ProducerConfig.ACKS_CONFIG, "1"); // Leader acknowledge
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // Compression pour optimiser le réseau
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Petite latence pour batch
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32MB
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);


        this.producer = new KafkaProducer<>(props);

        log.info("KafkaSenderService initialized with bootstrapServer={}, topic={}",
                bootstrapServer, this.topic);
    }

    @Override
    public boolean send(SendableRequestDto payload) {
        try {
            String key = generateKey(payload);
            String json = gson.toJson(payload);

            ProducerRecord<String, String> messageRecord = new ProducerRecord<>(this.topic, key, json);

            // Add authentication headers to the Kafka message
            addCredentialHeaders(messageRecord.headers());

            // Envoi asynchrone avec callback
            producer.send(messageRecord, (metadata, exception) -> {
                if (exception != null) {
                    log.error("KafkaSenderService - Async send failed for topic={}, key={}: {}",
                            topic, key, exception.getMessage());
                } else {
                    log.debug("KafkaSenderService - Message sent to topic={}, partition={}, offset={}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
            return true; // On retourne true immédiatement en mode async


        } catch (Exception e) {
            log.error("KafkaSenderService - Error sending payload: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Add credential headers to Kafka message headers
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

                log.trace("KafkaSenderService - Added {} credential headers to message",
                    credentials.size());
            }
        } catch (Exception e) {
            log.error("KafkaSenderService - Error adding credential headers: {}",
                e.getMessage(), e);
        }
    }

    /**
     * Generate a partition key for better distribution
     * Using appKey as partition key to ensure messages from same app go to same partition
     */
    private String generateKey(SendableRequestDto payload) {
        if (payload instanceof LogRequestDto logRequest) {
            return logRequest.appKey();
        } else if (payload instanceof MetricRequestDto metricRequest) {
            return metricRequest.appKey();
        }
        return "default";
    }

    /**
     * Flush pending messages and close producer
     * Should be called on application shutdown
     */
    public void close() {
        try {
            log.info("Closing KafkaSenderService - flushing pending messages");
            producer.flush();
            producer.close(java.time.Duration.ofSeconds(10));
            log.info("KafkaSenderService closed successfully");
        } catch (Exception e) {
            log.error("Error closing KafkaSenderService: {}", e.getMessage(), e);
        }
    }

    /**
     * Flush all pending messages
     */
    public void flush() {
        try {
            producer.flush();
            log.debug("KafkaSenderService - Messages flushed");
        } catch (Exception e) {
            log.error("KafkaSenderService - Error flushing messages: {}", e.getMessage(), e);
        }
    }
}
