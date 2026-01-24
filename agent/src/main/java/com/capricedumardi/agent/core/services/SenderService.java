package com.capricedumardi.agent.core.services;

import com.capricedumardi.agent.core.model.SendableRequestDto;

public interface SenderService extends AutoCloseable {
    /**
     * Send data to backend.
     *
     * This method must be thread-safe and non-blocking (or have reasonable timeout).
     *
     * @param payload The data to send (logs, metrics, etc.)
     * @return true if send succeeded, false otherwise
     *
     * Success criteria:
     * - HTTP: Response code 2xx
     * - Kafka: Message accepted by broker (if sync) or queued (if async with tracking)
     *
     * Failure cases:
     * - Network errors (timeout, connection refused, etc.)
     * - Server errors (5xx responses)
     * - Client errors
     * - Serialization errors
     *
     * Note: Implementations should NOT throw exceptions. Return false instead.
     */
    boolean send(SendableRequestDto payload);

    /**
     * Release resources held by this sender.
     *
     * This method should:
     * - Close HTTP connections and release connection pool
     * - Close Kafka producer and flush pending messages
     * - Shutdown background threads
     * - Release any other resources any (file handles, sockets, etc.)
     *
     * After close() is called, send() should return false immediately.
     *
     * Implementation note: This method is called by BuffersFactory during
     * shutdown, so it should complete within a reasonable time (< 30 seconds).
     */
    @Override
    void close();

    String getDescription();
}
