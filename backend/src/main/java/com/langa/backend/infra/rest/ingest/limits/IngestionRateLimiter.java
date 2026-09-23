package com.langa.backend.infra.rest.ingest.limits;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory fixed-window rate limiter (one window per minute and per key).
 * Per backend instance: with several instances the effective limit is multiplied accordingly.
 */
public class IngestionRateLimiter {

    static final long WINDOW_MILLIS = 60_000;
    /** Above this number of tracked keys, windows of previous minutes are purged. */
    private static final int CLEANUP_THRESHOLD = 10_000;

    private record Window(long start, AtomicInteger count) {
    }

    private final int requestsPerWindow;
    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public IngestionRateLimiter(int requestsPerWindow, Clock clock) {
        this.requestsPerWindow = requestsPerWindow;
        this.clock = clock;
    }

    /** @return true when the request is allowed, false when the key exceeded its quota for the current minute */
    public boolean tryAcquire(String key) {
        if (requestsPerWindow <= 0) {
            return true;
        }
        final long windowStart = clock.millis() / WINDOW_MILLIS * WINDOW_MILLIS;
        if (windows.size() > CLEANUP_THRESHOLD) {
            windows.values().removeIf(window -> window.start() < windowStart);
        }
        final Window window = windows.compute(key, (k, current) ->
                current == null || current.start() != windowStart ? new Window(windowStart, new AtomicInteger()) : current);
        return window.count().incrementAndGet() <= requestsPerWindow;
    }

    /** Seconds until the current window ends. */
    public long retryAfterSeconds() {
        final long elapsed = clock.millis() % WINDOW_MILLIS;
        return Math.max(1, (WINDOW_MILLIS - elapsed + 999) / 1000);
    }
}
