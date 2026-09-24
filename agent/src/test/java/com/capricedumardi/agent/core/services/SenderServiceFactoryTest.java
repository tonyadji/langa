package com.capricedumardi.agent.core.services;

import com.capricedumardi.agent.core.helpers.IngestionParamsResolver;
import com.capricedumardi.agent.testsupport.AgentTestSupport;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SenderServiceFactoryTest {

    private static String encodedCredentials(String accountKey, String appKey) {
        return Base64.getEncoder().encodeToString((accountKey + "-lga-" + appKey).getBytes());
    }

    @Test
    void createsHttpSenderForHttpEndpoint() {
        IngestionParamsResolver resolver = new IngestionParamsResolver(
                "https://host.example/api/ingestion/http-endpoint/" + encodedCredentials("acc", "app"),
                "secret"
        );

        SenderService sender = SenderServiceFactory.create(resolver, AgentTestSupport.dynamicConfig());
        try {
            assertInstanceOf(HttpSenderService.class, sender);
            assertTrue(sender.getDescription().contains("host.example"));
        } finally {
            sender.close();
        }
    }

    @Test
    void createsKafkaSenderForKafkaEndpoint() {
        IngestionParamsResolver resolver = new IngestionParamsResolver(
                "kafka://localhost:9092/api/ingestion/my-topic/" + encodedCredentials("acc", "app"),
                "secret"
        );

        SenderService sender = SenderServiceFactory.create(resolver, AgentTestSupport.dynamicConfig());
        try {
            assertInstanceOf(KafkaSenderService.class, sender);
            assertTrue(sender.getDescription().contains("my-topic"));
        } finally {
            sender.close();
        }
    }

    @Test
    void fallsBackToNoOpWhenCredentialsAreMalformed() {
        // Valid URL shape (constructor accepts it) but no credentials segment at all,
        // so resolveAppKey()/resolveAccountKey() blow up during validation.
        IngestionParamsResolver resolver = new IngestionParamsResolver(
                "https://host.example/api/ingestion/http-endpoint",
                "secret"
        );

        SenderService sender = SenderServiceFactory.create(resolver, AgentTestSupport.dynamicConfig());
        try {
            assertInstanceOf(NoOpSenderService.class, sender);
        } finally {
            sender.close();
        }
    }

    @Test
    void createNoOpReturnsNoOpSenderService() {
        SenderService sender = SenderServiceFactory.createNoOp("manual reason");

        assertInstanceOf(NoOpSenderService.class, sender);
        sender.close();
    }
}
