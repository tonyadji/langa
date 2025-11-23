package com.capricedumardi.agent.core.services;

import com.capricedumardi.agent.core.config.LangaPrinter;
import com.capricedumardi.agent.core.helpers.CredentialsHelper;
import com.capricedumardi.agent.core.helpers.IngestionParamsResolver;
import com.capricedumardi.agent.core.model.SenderType;

/**
 * Factory for creating SenderService instances
 * Supports both HTTP and Kafka implementations wit falling back to No-Op service
 */
public class SenderServiceFactory {

    private SenderServiceFactory() {
    }

    public static SenderService create(final IngestionParamsResolver resolver) {
        LangaPrinter.printTrace("Creating SenderService from configuration...");

        try {
            validateConfiguration(resolver);

            SenderType senderType = resolver.resolveSenderType();

            CredentialsHelper credentialsHelper = createCredentialsHelper(resolver);

            SenderService sender = createSender(senderType, resolver, credentialsHelper);

            LangaPrinter.printTrace("SenderService created successfully: " + sender.getDescription());
            return sender;

        } catch (IllegalArgumentException | IllegalStateException e) {
            LangaPrinter.printError("FATAL: Failed to create SenderService");

            return createNoOp("Falling back to No-Op service because : " + e.getMessage());

        } catch (Exception e) {
            LangaPrinter.printError("FATAL: Unexpected error creating SenderService");
            LangaPrinter.printError("Error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace(System.err);

            return createNoOp("Falling back to No-Op service because : " + e.getMessage());
        }
    }

    /**
     * Validate configuration before attempting to create sender.
     *
     * @throws IllegalArgumentException if any required parameter is missing/invalid
     */
    private static void validateConfiguration(IngestionParamsResolver resolver) {
        String ingestionUrl = resolver.ingestionUrl();
        if (ingestionUrl == null || ingestionUrl.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "LANGA_URL is required but not configured. " +
                            "Set environment variable: LANGA_URL=<your-ingestion-url>"
            );
        }

        String secret = resolver.resolveSecret();
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "LANGA_SECRET is required but not configured. " +
                            "Set environment variable: LANGA_SECRET=<your-secret>"
            );
        }

        String appKey = resolver.resolveAppKey();
        if (appKey == null || appKey.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "AppKey could not be extracted from LANGA_URL. " +
                            "Check URL format: .../api/ingestion/{type}/{base64_credentials}"
            );
        }

        String accountKey = resolver.resolveAccountKey();
        if (accountKey == null || accountKey.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "AccountKey could not be extracted from LANGA_URL. " +
                            "Check URL format: .../api/ingestion/{type}/{base64_credentials}"
            );
        }

        LangaPrinter.printTrace("  Configuration validated successfully");
    }

    /**
     * Create credentials helper with validated parameters.
     */
    private static CredentialsHelper createCredentialsHelper(IngestionParamsResolver resolver) {
        String appKey = resolver.resolveAppKey();
        String accountKey = resolver.resolveAccountKey();
        String secret = resolver.resolveSecret();

        return CredentialsHelper.of(appKey, accountKey, secret);
    }

    /**
     * Create sender based on type.
     */
    private static SenderService createSender(SenderType type,
                                              IngestionParamsResolver resolver,
                                              CredentialsHelper credentialsHelper) {
        return switch (type) {
            case HTTP -> createHttpSender(resolver, credentialsHelper);
            case KAFKA -> createKafkaSender(resolver, credentialsHelper);
        };
    }

    /**
     * Create HTTP sender with configuration validation.
     *
     * @throws IllegalArgumentException if HTTP-specific configuration is invalid
     */
    private static SenderService createHttpSender(IngestionParamsResolver resolver,
                                                  CredentialsHelper credentialsHelper) {
        String url = resolver.resolveHttpUrl();

        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("HTTP URL could not be resolved from configuration");
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException(
                    "Invalid HTTP URL format: " + url + ". Must start with http:// or https://"
            );
        }

        LangaPrinter.printTrace("  HTTP URL: " + url);
        return new HttpSenderService(url, credentialsHelper);
    }

    /**
     * Create Kafka sender with configuration validation.
     *
     * @throws IllegalArgumentException if Kafka-specific configuration is invalid
     */
    private static SenderService createKafkaSender(IngestionParamsResolver resolver,
                                                   CredentialsHelper credentialsHelper) {
        String bootstrapServer = resolver.resolveBootStrapServer();
        String topic = resolver.resolveTopic();

        if (bootstrapServer == null || bootstrapServer.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Kafka bootstrap server could not be resolved from configuration"
            );
        }

        if (topic == null || topic.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Kafka topic could not be resolved from configuration"
            );
        }

        if (!bootstrapServer.contains(":")) {
            LangaPrinter.printError("WARNING: Bootstrap server might be missing port: " + bootstrapServer);
            LangaPrinter.printError("Expected format: host:port (e.g., localhost:9092)");
        }

        LangaPrinter.printTrace("Bootstrap Server: " + bootstrapServer);
        LangaPrinter.printTrace("Topic: " + topic);

        return new KafkaSenderService(bootstrapServer, topic, credentialsHelper);
    }

    /**
     * Create a no-op sender for testing or fallback scenarios.
     *
     * @param reason Human-readable reason why no-op sender is being used
     * @return NoOpSenderService instance
     */
    public static SenderService createNoOp(String reason) {
        return new NoOpSenderService(reason);
    }
}

