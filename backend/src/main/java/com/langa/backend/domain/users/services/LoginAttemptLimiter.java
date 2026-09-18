package com.langa.backend.domain.users.services;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginAttemptLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);

    private final Map<String, Attempt> attemptsByUsername = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        Attempt attempt = attemptsByUsername.get(normalize(username));
        return attempt != null && attempt.isLocked();
    }

    public void recordFailure(String username) {
        attemptsByUsername.compute(normalize(username), (key, current) -> {
            int count = (current == null ? 0 : current.count()) + 1;
            Instant lockedUntil = count >= MAX_ATTEMPTS ? Instant.now().plus(LOCKOUT_DURATION) : null;
            return new Attempt(count, lockedUntil);
        });
    }

    public void recordSuccess(String username) {
        attemptsByUsername.remove(normalize(username));
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private record Attempt(int count, Instant lockedUntil) {
        boolean isLocked() {
            return lockedUntil != null && Instant.now().isBefore(lockedUntil);
        }
    }
}
