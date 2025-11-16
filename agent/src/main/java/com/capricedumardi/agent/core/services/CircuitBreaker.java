package com.capricedumardi.agent.core.services;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit breaker implementation to prevent wasting time on failed backends.
 *
 * States:
 * - CLOSED: Normal operation, requests pass through
 * - OPEN: Too many failures, requests are immediately rejected
 * - HALF_OPEN: Testing if backend recovered, limited requests allowed
 *
 * Transitions:
 * CLOSED -> OPEN: After N consecutive failures
 * OPEN -> HALF_OPEN: After timeout period
 * HALF_OPEN -> CLOSED: After successful test request
 * HALF_OPEN -> OPEN: If test request fails
 *
 * Thread-safe using atomic operations.
 */
public class CircuitBreaker {

    public enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    private final int failureThreshold;
    private final long openDurationMillis;
    private final String name;

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final AtomicLong lastStateChangeTime = new AtomicLong(System.currentTimeMillis());


    public CircuitBreaker(String name, int failureThreshold, long openDurationMillis) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.openDurationMillis = openDurationMillis;
    }


    public boolean allowRequest() {
        State currentState = state.get();

        switch (currentState) {
            case CLOSED:
                return true;

            case OPEN:
                long timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime.get();
                if (timeSinceLastFailure >= openDurationMillis) {
                    if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                        lastStateChangeTime.set(System.currentTimeMillis());
                        System.out.println("CircuitBreaker[" + name + "]: OPEN -> HALF_OPEN (testing recovery)");
                        return true;
                    }
                }
                return false;

            case HALF_OPEN:
                return true;

            default:
                return true;
        }
    }

    public void recordSuccess() {
        consecutiveFailures.set(0);

        State currentState = state.get();
        if (currentState == State.HALF_OPEN) {
            if (state.compareAndSet(State.HALF_OPEN, State.CLOSED)) {
                lastStateChangeTime.set(System.currentTimeMillis());
                System.out.println("CircuitBreaker[" + name + "]: HALF_OPEN -> CLOSED (recovery confirmed)");
            }
        }
    }

    public void recordFailure() {
        lastFailureTime.set(System.currentTimeMillis());
        int failures = consecutiveFailures.incrementAndGet();

        State currentState = state.get();

        if (currentState == State.HALF_OPEN) {
            if (state.compareAndSet(State.HALF_OPEN, State.OPEN)) {
                lastStateChangeTime.set(System.currentTimeMillis());
                System.err.println("CircuitBreaker[" + name + "]: HALF_OPEN -> OPEN (test failed)");
            }
        } else if (currentState == State.CLOSED && failures >= failureThreshold) {
            if (state.compareAndSet(State.CLOSED, State.OPEN)) {
                lastStateChangeTime.set(System.currentTimeMillis());
                System.err.println("CircuitBreaker[" + name + "]: CLOSED -> OPEN (threshold exceeded: " +
                        failures + " failures)");
            }
        }
    }

    public State getState() {
        return state.get();
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures.get();
    }

    public boolean isClosed() {
        return state.get() == State.CLOSED;
    }

    public boolean isOpen() {
        return state.get() == State.OPEN;
    }

    public void reset() {
        state.set(State.CLOSED);
        consecutiveFailures.set(0);
        lastStateChangeTime.set(System.currentTimeMillis());
        System.out.println("CircuitBreaker[" + name + "]: Manually reset to CLOSED");
    }

    public long getTimeSinceLastStateChange() {
        return System.currentTimeMillis() - lastStateChangeTime.get();
    }

    @Override
    public String toString() {
        return String.format("CircuitBreaker[%s: state=%s, failures=%d, timeSinceChange=%dms]",
                name, state.get(), consecutiveFailures.get(), getTimeSinceLastStateChange());
    }
}