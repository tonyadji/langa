package com.capricedumardi.agent.core.services;

import com.capricedumardi.agent.core.helpers.CredentialsHelper;
import com.capricedumardi.agent.core.model.LogRequestDto;
import com.capricedumardi.agent.core.model.MetricRequestDto;
import com.capricedumardi.agent.core.model.SendableRequestDto;
import com.capricedumardi.agent.core.model.SendableRequestType;
import com.capricedumardi.agent.testsupport.AgentTestSupport;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KafkaSenderServiceTest {

    private KafkaSenderService newSender() {
        CredentialsHelper credentials = CredentialsHelper.of("appKey", "accountKey", "secret");

        return new KafkaSenderService("localhost:9092", "my-topic", credentials, AgentTestSupport.dynamicConfig());
    }

    @Test
    void constructionSucceedsWithoutAReachableBroker() {
        KafkaSenderService sender = newSender();
        try {
            assertEquals(CircuitBreaker.State.CLOSED, sender.getCircuitBreakerState());
        } finally {
            sender.close();
        }
    }

    @Test
    void descriptionContainsTopicAndMode() {
        KafkaSenderService sender = newSender();
        try {
            String description = sender.getDescription();
            assertTrue(description.contains("my-topic"));
            assertTrue(description.contains("CLOSED"));
        } finally {
            sender.close();
        }
    }

    @Test
    void closeIsIdempotent() {
        KafkaSenderService sender = newSender();

        assertDoesNotThrow(sender::close);
        assertDoesNotThrow(sender::close);
    }

    @Test
    void generateKeyUsesAppKeyForLogRequestDto() throws Exception {
        KafkaSenderService sender = newSender();
        try {
            SendableRequestDto dto = new LogRequestDto("theAppKey", "acc", List.of(), SendableRequestType.LOG);
            assertEquals("theAppKey", invokeGenerateKey(sender, dto));
        } finally {
            sender.close();
        }
    }

    @Test
    void generateKeyUsesAppKeyForMetricRequestDto() throws Exception {
        KafkaSenderService sender = newSender();
        try {
            SendableRequestDto dto = new MetricRequestDto("theAppKey", "acc", List.of(), SendableRequestType.METRIC);
            assertEquals("theAppKey", invokeGenerateKey(sender, dto));
        } finally {
            sender.close();
        }
    }

    @Test
    void generateKeyFallsBackToDefaultForUnknownPayloadType() throws Exception {
        KafkaSenderService sender = newSender();
        try {
            SendableRequestDto dto = new SendableRequestDto() { };
            assertEquals("default", invokeGenerateKey(sender, dto));
        } finally {
            sender.close();
        }
    }

    private static String invokeGenerateKey(KafkaSenderService sender, SendableRequestDto dto) throws Exception {
        Method method = KafkaSenderService.class.getDeclaredMethod("generateKey", SendableRequestDto.class);
        method.setAccessible(true);
        return (String) method.invoke(sender, dto);
    }
}
