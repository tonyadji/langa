package com.capricedumardi.agent.core.services;

import com.capricedumardi.agent.core.helpers.CredentialsHelper;
import com.capricedumardi.agent.core.helpers.IngestionParamsResolver;
import com.capricedumardi.agent.core.model.SenderType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Factory for creating SenderService instances
 * Supports both HTTP and Kafka implementations
 */
public class SenderServiceFactory {
    private static final Logger log = LogManager.getLogger(SenderServiceFactory.class);
    public static final String URL = "url";
    public static final String KAFKA_BOOTSTRAP_SERVER = "bootstrapServer";
    public static final String TOPIC = "topic";

    private SenderServiceFactory() {
    }


    public static SenderService create(final IngestionParamsResolver resolver) {
        try {
            final String ingestionUrl = resolver.ingestionUrl();
            final String secret = resolver.resolveSecret();
            if (ingestionUrl == null || ingestionUrl.isEmpty()) {
                log.warn("LANGA_URL environment variable or property langa.url is not set, using default value {}", ingestionUrl);
                return new NoOpSenderService("LANGA_URL environment variable or property langa.url is not set");
            }
            if (secret == null || secret.isEmpty()) {
                log.warn("LANGA_SECRET environment variable or property langa.secret is not set{}", secret);
                return new NoOpSenderService("LANGA_SECRET environment variable or property langa.secret is not set");
            }
            SenderType senderType = resolver.resolveSenderType();

            Map<String, String> config = switch (senderType) {
                case HTTP -> Map.of(
                        URL, resolver.resolveHttpUrl()
                );
                case KAFKA -> Map.of(
                        KAFKA_BOOTSTRAP_SERVER, resolver.resolveBootStrapServer(),
                        TOPIC, resolver.resolveTopic()
                );
            };
            log.trace("Creating SenderService from environment: type={}", senderType);
            return create(senderType, config, CredentialsHelper.of(resolver.resolveAppKey(), resolver.resolveAccountKey(), resolver.resolveSecret()));
        } catch (Exception e) {
            return new NoOpSenderService(e.getMessage());
        }
    }

    /**
     * Create a SenderService based on type
     *
     * @param type              The type of sender (HTTP or KAFKA)
     * @param config            Configuration map with required parameters
     * @param credentialsHelper
     * @return SenderService instance
     */
    private static SenderService create(SenderType type, Map<String, String> config,
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
        String url = config.get(URL);
        log.trace("Creating HttpSenderService with url={}", url);
        return new HttpSenderService(url, credentialsHelper);
    }

    /**
     * Create Kafka sender
     * Required config keys: bootstrapServers, topic
     * Optional: asyncSend (default: true)
     */
    private static SenderService createKafkaSender(Map<String, String> config,
                                                   CredentialsHelper credentialsHelper) {
        String bootstrapServers = config.get(KAFKA_BOOTSTRAP_SERVER);
        String topic = config.get(TOPIC);

        if (bootstrapServers == null || bootstrapServers.isEmpty()) {
            return new NoOpSenderService("Kafka sender requires 'bootstrapServers' parameter");
        }
        if (topic == null || topic.isEmpty()) {
            return new NoOpSenderService("Kafka sender requires 'topic' parameter");
        }

        log.trace("Creating KafkaSenderService with bootstrapServer={}, topic={}",
                bootstrapServers, topic);

        return new KafkaSenderService(bootstrapServers, topic, credentialsHelper);
    }
}

