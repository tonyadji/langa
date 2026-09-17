package com.capricedumardi.agent.core.helpers;

import com.capricedumardi.agent.core.model.SenderType;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IngestionParamsResolverTest {

    private static String encodedCredentials(String accountKey, String appKey) {
        return Base64.getEncoder().encodeToString((accountKey + "-lga-" + appKey).getBytes());
    }

    @Test
    void constructorRejectsNullOrEmptyUrl() {
        assertThrows(IllegalArgumentException.class, () -> new IngestionParamsResolver(null, "secret"));
        assertThrows(IllegalArgumentException.class, () -> new IngestionParamsResolver("  ", "secret"));
    }

    @Test
    void constructorRejectsNullOrEmptySecret() {
        assertThrows(IllegalArgumentException.class,
                () -> new IngestionParamsResolver("https://host/api/ingestion/http/creds", null));
        assertThrows(IllegalArgumentException.class,
                () -> new IngestionParamsResolver("https://host/api/ingestion/http/creds", " "));
    }

    @Test
    void constructorRejectsUrlWithoutIngestionEndpoint() {
        assertThrows(IllegalArgumentException.class,
                () -> new IngestionParamsResolver("https://host/wrong/path", "secret"));
    }

    @Test
    void httpUrlIsResolvedFromHttpPrefixedCredentials() {
        String creds = encodedCredentials("acc", "app");
        IngestionParamsResolver resolver =
                new IngestionParamsResolver("https://host.example/api/ingestion/http-endpoint/" + creds, "secret");

        assertEquals(SenderType.HTTP, resolver.resolveSenderType());
        assertEquals("https://host.example/api/ingestion", resolver.resolveHttpUrl());
        assertEquals("app", resolver.resolveAppKey());
        assertEquals("acc", resolver.resolveAccountKey());
        assertEquals("secret", resolver.resolveSecret());
    }

    @Test
    void kafkaSenderTypeIsResolvedWhenSegmentDoesNotStartWithH() {
        String creds = encodedCredentials("acc", "app");
        IngestionParamsResolver resolver =
                new IngestionParamsResolver("kafka://broker:9092/api/ingestion/my-topic/" + creds, "secret");

        assertEquals(SenderType.KAFKA, resolver.resolveSenderType());
    }

    @Test
    void bootstrapServerStripsKafkaScheme() {
        String creds = encodedCredentials("acc", "app");
        IngestionParamsResolver resolver =
                new IngestionParamsResolver("kafka://broker:9092/api/ingestion/my-topic/" + creds, "secret");

        assertEquals("broker:9092", resolver.resolveBootStrapServer());
    }

    @Test
    void bootstrapServerKeptAsIsWithoutKafkaScheme() {
        String creds = encodedCredentials("acc", "app");
        IngestionParamsResolver resolver =
                new IngestionParamsResolver("broker:9092/api/ingestion/my-topic/" + creds, "secret");

        assertEquals("broker:9092", resolver.resolveBootStrapServer());
    }

    @Test
    void topicIsResolvedFromFirstSegmentAfterEndpoint() {
        String creds = encodedCredentials("acc", "app");
        IngestionParamsResolver resolver =
                new IngestionParamsResolver("kafka://broker:9092/api/ingestion/my-topic/" + creds, "secret");

        assertEquals("my-topic", resolver.resolveTopic());
    }

    @Test
    void appKeyAndAccountKeyAreDecodedFromBase64Credentials() {
        String creds = encodedCredentials("myAccount", "myApp");
        IngestionParamsResolver resolver =
                new IngestionParamsResolver("https://host/api/ingestion/http/" + creds, "secret");

        assertEquals("myApp", resolver.resolveAppKey());
        assertEquals("myAccount", resolver.resolveAccountKey());
    }

    @Test
    void resolvingAppKeyThrowsWhenUrlHasNoCredentialsSegment() {
        IngestionParamsResolver resolver =
                new IngestionParamsResolver("https://host/api/ingestion/http", "secret");

        assertThrows(IllegalStateException.class, resolver::resolveAppKey);
    }

    @Test
    void resolvingAppKeyThrowsOnInvalidBase64() {
        IngestionParamsResolver resolver =
                new IngestionParamsResolver("https://host/api/ingestion/http/not-valid-base64!!!", "secret");

        assertThrows(IllegalStateException.class, resolver::resolveAppKey);
    }

    @Test
    void resolvingAppKeyThrowsWhenDecodedCredentialsHaveWrongFormat() {
        String badCreds = Base64.getEncoder().encodeToString("no-delimiter-here".getBytes());
        IngestionParamsResolver resolver =
                new IngestionParamsResolver("https://host/api/ingestion/http/" + badCreds, "secret");

        assertThrows(IllegalStateException.class, resolver::resolveAppKey);
    }

    @Test
    void resolvingTopicThrowsWhenUrlHasNoTopicSegment() {
        IngestionParamsResolver resolver =
                new IngestionParamsResolver("kafka://broker:9092/api/ingestion/", "secret");

        assertThrows(IllegalStateException.class, resolver::resolveTopic);
    }
}
