package com.langa.backend.infra.rest.ingest.limits;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class IngestionLimitsFilterTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T10:00:30Z"), ZoneOffset.UTC);

    private static IngestionLimitsFilter filter(long maxPayloadBytes, int requestsPerMinute) {
        IngestionLimitsProperties properties = new IngestionLimitsProperties();
        properties.setMaxPayloadBytes(maxPayloadBytes);
        properties.setRequestsPerMinute(requestsPerMinute);
        return new IngestionLimitsFilter(properties, new IngestionRateLimiter(requestsPerMinute, CLOCK));
    }

    private static MockHttpServletRequest request(String appKey, byte[] body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/ingestion");
        request.addHeader(IngestionLimitsFilter.APP_KEY_HEADER, appKey);
        request.setContent(body);
        return request;
    }

    @Test
    void shouldLetAReasonableRequestThrough() throws Exception {
        AtomicReference<HttpServletRequest> forwarded = new AtomicReference<>();
        FilterChain chain = (req, res) -> forwarded.set((HttpServletRequest) req);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter(100, 10).doFilter(request("APP-1", "{}".getBytes()), response, chain);

        assertNotNull(forwarded.get());
        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldRejectADeclaredPayloadAboveTheLimit() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter(10, 10).doFilter(request("APP-1", new byte[11]), response,
                (req, res) -> fail("the request must not reach the application"));

        assertEquals(413, response.getStatus());
        assertTrue(response.getContentAsString().contains("413-000"));
    }

    @Test
    void shouldStopReadingAnUndeclaredPayloadAboveTheLimit() throws Exception {
        // Chunked body: no Content-Length declared
        MockHttpServletRequest chunked = new MockHttpServletRequest("POST", "/api/ingestion") {
            @Override
            public long getContentLengthLong() {
                return -1;
            }
        };
        chunked.setContent(new byte[50]);

        FilterChain chain = (req, res) -> {
            InputStream body = req.getInputStream();
            assertThrows(IngestionPayloadTooLargeException.class, () -> body.readAllBytes());
        };

        filter(10, 10).doFilter(chunked, new MockHttpServletResponse(), chain);
    }

    @Test
    void shouldRateLimitPerApplicationKey() throws Exception {
        IngestionLimitsFilter filter = filter(100, 2);
        FilterChain chain = (req, res) -> { };

        MockHttpServletResponse first = new MockHttpServletResponse();
        MockHttpServletResponse second = new MockHttpServletResponse();
        MockHttpServletResponse third = new MockHttpServletResponse();
        MockHttpServletResponse otherApp = new MockHttpServletResponse();
        filter.doFilter(request("APP-1", "{}".getBytes()), first, chain);
        filter.doFilter(request("APP-1", "{}".getBytes()), second, chain);
        filter.doFilter(request("APP-1", "{}".getBytes()), third, chain);
        filter.doFilter(request("APP-2", "{}".getBytes()), otherApp, chain);

        assertEquals(200, first.getStatus());
        assertEquals(200, second.getStatus());
        assertEquals(429, third.getStatus());
        assertEquals("30", third.getHeader("Retry-After"));
        assertTrue(third.getContentAsString().contains("429-001"));
        assertEquals(200, otherApp.getStatus(), "each application has its own quota");
    }

    @Test
    void rateLimiter_shouldResetOnTheNextMinuteAndBeDisabledWithZero() {
        Clock[] now = {Clock.fixed(Instant.parse("2026-01-01T10:00:59Z"), ZoneOffset.UTC)};
        IngestionRateLimiter limiter = new IngestionRateLimiter(1, new Clock() {
            @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
            @Override public Clock withZone(java.time.ZoneId zone) { return this; }
            @Override public Instant instant() { return now[0].instant(); }
        });

        assertTrue(limiter.tryAcquire("APP-1"));
        assertFalse(limiter.tryAcquire("APP-1"));
        now[0] = Clock.fixed(Instant.parse("2026-01-01T10:01:00Z"), ZoneOffset.UTC);
        assertTrue(limiter.tryAcquire("APP-1"));

        IngestionRateLimiter disabled = new IngestionRateLimiter(0, CLOCK);
        for (int i = 0; i < 1000; i++) {
            assertTrue(disabled.tryAcquire("APP-1"));
        }
    }
}
