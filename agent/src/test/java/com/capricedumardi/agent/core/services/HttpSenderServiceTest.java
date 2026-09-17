package com.capricedumardi.agent.core.services;

import com.capricedumardi.agent.core.helpers.CredentialsHelper;
import com.capricedumardi.agent.core.model.LogRequestDto;
import com.capricedumardi.agent.core.model.SendableRequestType;
import com.capricedumardi.agent.testsupport.AgentTestSupport;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpSenderServiceTest {

    private static HttpServer server;
    private static String baseUrl;
    private static final AtomicInteger requestCount = new AtomicInteger();
    private static final Queue<Integer> statusesToReturn = new ConcurrentLinkedQueue<>();
    private static volatile String lastContentEncoding;

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/ingest", HttpSenderServiceTest::handle);
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort() + "/ingest";
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @AfterEach
    void resetServerState() {
        requestCount.set(0);
        statusesToReturn.clear();
        lastContentEncoding = null;
        AgentTestSupport.dynamicConfig().enableHttpCompression(false);
    }

    private static void handle(HttpExchange exchange) throws IOException {
        requestCount.incrementAndGet();
        lastContentEncoding = exchange.getRequestHeaders().getFirst("Content-Encoding");
        exchange.getRequestBody().readAllBytes();

        Integer status = statusesToReturn.poll();
        int code = status != null ? status : 200;

        byte[] body = "{}".getBytes();
        exchange.sendResponseHeaders(code, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private HttpSenderService newSender() {
        CredentialsHelper credentials = CredentialsHelper.of("appKey", "accountKey", "secret");
        return new HttpSenderService(baseUrl, credentials, AgentTestSupport.dynamicConfig());
    }

    private LogRequestDto payload() {
        return new LogRequestDto("appKey", "accountKey", List.of(), SendableRequestType.LOG);
    }

    @Test
    void successfulSendReturnsTrueAndRecordsSuccess() {
        HttpSenderService sender = newSender();
        try {
            boolean result = sender.send(payload());

            assertTrue(result);
            assertEquals(1, requestCount.get());
            assertEquals(1, sender.getTotalSent());
            assertEquals(0, sender.getTotalFailed());
            assertEquals(CircuitBreaker.State.CLOSED, sender.getCircuitBreakerState());
        } finally {
            sender.close();
        }
    }

    @Test
    void clientErrorFailsImmediatelyWithoutRetrying() {
        statusesToReturn.add(400);
        HttpSenderService sender = newSender();
        try {
            boolean result = sender.send(payload());

            assertFalse(result);
            assertEquals(1, requestCount.get(), "4xx (other than 429) must not be retried");
            assertEquals(1, sender.getTotalFailed());
        } finally {
            sender.close();
        }
    }

    @Test
    void serverErrorIsRetriedUpToMaxAttempts() {
        statusesToReturn.add(500);
        statusesToReturn.add(500);
        statusesToReturn.add(500);
        HttpSenderService sender = newSender();
        try {
            boolean result = sender.send(payload());

            assertFalse(result);
            int maxAttempts = AgentTestSupport.dynamicConfig().getHttpMaxRetryAttempts();
            assertEquals(maxAttempts, requestCount.get());
        } finally {
            sender.close();
        }
    }

    @Test
    void succeedsAfterATransientServerError() {
        statusesToReturn.add(500);
        statusesToReturn.add(200);
        HttpSenderService sender = newSender();
        try {
            boolean result = sender.send(payload());

            assertTrue(result);
            assertEquals(2, requestCount.get());
        } finally {
            sender.close();
        }
    }

    @Test
    void compressionIsAppliedWhenEnabledAndAboveThreshold() {
        AgentTestSupport.dynamicConfig().enableHttpCompression(true);
        AgentTestSupport.dynamicConfig().setHttpCompressionThresholdBytes(1);

        HttpSenderService sender = newSender();
        try {
            boolean result = sender.send(payload());

            assertTrue(result);
            assertEquals("gzip", lastContentEncoding);
            assertEquals(1, sender.getTotalCompressed());
        } finally {
            sender.close();
        }
    }

    @Test
    void closeIsIdempotentAndPreventsFurtherSends() {
        HttpSenderService sender = newSender();

        sender.close();
        sender.close();

        assertFalse(sender.send(payload()));
        assertEquals(0, requestCount.get());
    }

    @Test
    void descriptionContainsUrlAndCircuitState() {
        HttpSenderService sender = newSender();
        try {
            String description = sender.getDescription();
            assertTrue(description.contains(baseUrl));
            assertTrue(description.contains("CLOSED"));
        } finally {
            sender.close();
        }
    }
}
