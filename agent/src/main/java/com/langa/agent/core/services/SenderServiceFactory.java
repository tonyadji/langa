package com.langa.agent.core.services;

import com.langa.agent.core.helpers.CredentialsHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Factory for creating SenderService instances
 * Supports both HTTP and Kafka implementations
 */
public class SenderServiceFactory {
    private static final Logger log = LogManager.getLogger(SenderServiceFactory.class);
    private static final String DEFAULT_INGESTION_URL = "http://localhost:8080/api/ingestion";

    public enum SenderType {
        HTTP,
        KAFKA
    }

    /**
     * Create a SenderService based on type
     *
     * @param type              The type of sender (HTTP or KAFKA)
     * @param config            Configuration map with required parameters
     * @param credentialsHelper
     * @return SenderService instance
     */
    public static SenderService create(SenderType type, Map<String, String> config,
        CredentialsHelper credentialsHelper) {
        switch (type) {
            case HTTP -> {
                return createHttpSender(config, credentialsHelper);
            }
            case KAFKA -> {
                return createKafkaSender(config, credentialsHelper);
            }
            default -> throw new IllegalArgumentException("Unknown sender type: " + type);
        }
    }

    /**
     * Create HTTP sender
     * Required config keys: url, appKey, accountKey, appSecret
     */
    private static SenderService createHttpSender(Map<String, String> config,
        CredentialsHelper credentialsHelper) {
        String url = config.get("url");
        if (url == null || url.isEmpty()) {
            return new HttpSenderService(DEFAULT_INGESTION_URL, credentialsHelper);
        }

        log.info("Creating HttpSenderService with url={}", url);
        return new HttpSenderService(url, credentialsHelper);
    }

    /**
     * Create Kafka sender
     * Required config keys: bootstrapServers, topic
     * Optional: asyncSend (default: true)
     */
    private static SenderService createKafkaSender(Map<String, String> config,
        CredentialsHelper credentialsHelper) {
        String bootstrapServers = config.get("bootstrapServer");
        if (bootstrapServers == null || bootstrapServers.isEmpty()) {
            return new NoOpSenderService("Kafka sender requires 'bootstrapServers' parameter");
        }

        String topic = config.getOrDefault("topic", "langa");

        log.info("Creating KafkaSenderService with bootstrapServer={}, topic={}",
                bootstrapServers, topic);

        return new KafkaSenderService(bootstrapServers, topic, credentialsHelper);
    }

    /**
     * Create SenderService from environment variables or system properties
     * Supported variables/properties:
     * - LANGA_SENDER_TYPE / langa.sender.type (http or kafka)
     * For HTTP:
     * - LANGA_HTTP_URL / langa.http.url
     * For Kafka:
     * - LANGA_KAFKA_BOOTSTRAP_SERVER / langa.kafka.bootstrap.server
     * - LANGA_KAFKA_LOGS_TOPIC / langa.kafka.topic
     */
    public static SenderService createFromEnvironmentAndCredentialHelper(CredentialsHelper credentialsHelper) {
        String senderTypeStr = getEnvOrProperty("LANGA_SENDER_TYPE", "langa.sender.type", "http");
        SenderType senderType = SenderType.valueOf(senderTypeStr.toUpperCase());

        Map<String, String> config = switch (senderType) {
            case HTTP -> Map.of(
                    "url", getEnvOrProperty("LANGA_HTTP_URL", "langa.http.url", "")
            );
            case KAFKA -> Map.of(
                    "bootstrapServer", getEnvOrProperty("LANGA_KAFKA_BOOTSTRAP_SERVER", "langa.kafka.bootstrap.server", "localhost:9092"),
                    "topic", getEnvOrProperty("LANGA_KAFKA_LOGS_TOPIC", "langa.kafka.topic", "langa-logs")
            );
        };

        log.info("Creating SenderService from environment: type={}", senderType);
        return create(senderType, config, credentialsHelper);
    }

    /**
     * Helper method to get value from environment variable or system property
     */
    private static String getEnvOrProperty(String envName, String propertyName, String defaultValue) {
        String value = System.getenv(envName);
        if (value == null || value.isEmpty()) {
            value = System.getProperty(propertyName, defaultValue);
        }
        return value;
    }
}

