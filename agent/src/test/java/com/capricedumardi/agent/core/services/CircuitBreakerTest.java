package com.capricedumardi.agent.core.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircuitBreakerTest {

    @Test
    void startsClosedAndAllowsRequests() {
        CircuitBreaker cb = new CircuitBreaker("test", 3, 1000);

        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
        assertTrue(cb.isClosed());
        assertFalse(cb.isOpen());
        assertTrue(cb.allowRequest());
        assertEquals(0, cb.getConsecutiveFailures());
    }

    @Test
    void opensAfterReachingFailureThreshold() {
        CircuitBreaker cb = new CircuitBreaker("test", 3, 1000);

        cb.recordFailure();
        cb.recordFailure();
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());

        cb.recordFailure();
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
        assertTrue(cb.isOpen());
        assertEquals(3, cb.getConsecutiveFailures());
    }

    @Test
    void rejectsRequestsWhileOpen() {
        CircuitBreaker cb = new CircuitBreaker("test", 1, 10_000);

        cb.recordFailure();
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
        assertFalse(cb.allowRequest());
    }

    @Test
    void successResetsConsecutiveFailuresWithoutReopening() {
        CircuitBreaker cb = new CircuitBreaker("test", 3, 1000);

        cb.recordFailure();
        cb.recordFailure();
        cb.recordSuccess();

        assertEquals(0, cb.getConsecutiveFailures());
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
    }

    @Test
    void transitionsToHalfOpenAfterOpenDurationElapses() throws InterruptedException {
        CircuitBreaker cb = new CircuitBreaker("test", 1, 50);

        cb.recordFailure();
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
        assertFalse(cb.allowRequest());

        Thread.sleep(80);

        assertTrue(cb.allowRequest());
        assertEquals(CircuitBreaker.State.HALF_OPEN, cb.getState());
    }

    @Test
    void halfOpenSuccessClosesCircuit() throws InterruptedException {
        CircuitBreaker cb = new CircuitBreaker("test", 1, 30);

        cb.recordFailure();
        Thread.sleep(50);
        assertTrue(cb.allowRequest());
        assertEquals(CircuitBreaker.State.HALF_OPEN, cb.getState());

        cb.recordSuccess();

        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
    }

    @Test
    void halfOpenFailureReopensCircuit() throws InterruptedException {
        CircuitBreaker cb = new CircuitBreaker("test", 1, 30);

        cb.recordFailure();
        Thread.sleep(50);
        assertTrue(cb.allowRequest());
        assertEquals(CircuitBreaker.State.HALF_OPEN, cb.getState());

        cb.recordFailure();

        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
    }

    @Test
    void resetForcesClosedState() {
        CircuitBreaker cb = new CircuitBreaker("test", 1, 10_000);

        cb.recordFailure();
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());

        cb.reset();

        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
        assertEquals(0, cb.getConsecutiveFailures());
    }

    @Test
    void getTimeSinceLastStateChangeIsNonNegative() {
        CircuitBreaker cb = new CircuitBreaker("test", 3, 1000);
        assertTrue(cb.getTimeSinceLastStateChange() >= 0);
    }

    @Test
    void toStringContainsNameAndState() {
        CircuitBreaker cb = new CircuitBreaker("my-breaker", 3, 1000);
        String s = cb.toString();
        assertTrue(s.contains("my-breaker"));
        assertTrue(s.contains("CLOSED"));
    }
}
