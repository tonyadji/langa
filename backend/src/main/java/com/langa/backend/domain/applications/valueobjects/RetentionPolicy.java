package com.langa.backend.domain.applications.valueobjects;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.exceptions.ApplicationException;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record RetentionPolicy(
        long duration,
        ChronoUnit unit,
        Instant lastUpdatedDate
) {
    private static final long MAX_DAYS = 45;
    private static final long MIN_DAYS = 1;

    public static RetentionPolicy defaultPolicy() {
        return new RetentionPolicy(MAX_DAYS, ChronoUnit.DAYS, null);
    }

    public RetentionPolicy update(RetentionPolicy requestedPolicy) {
        if (requestedPolicy == null) {
            return this;
        }

        long daysRequested;

        try {
            if (requestedPolicy.unit().isDurationEstimated()) {
                if (requestedPolicy.unit() == ChronoUnit.MONTHS) {
                    daysRequested = requestedPolicy.duration() * 30;
                } else if (requestedPolicy.unit() == ChronoUnit.YEARS) {
                    daysRequested = requestedPolicy.duration() * 365;
                } else {
                    throw new ApplicationException("Temporal Unit not supported", null, Errors.VALIDATION_ERROR);
                }
            } else {
                daysRequested = Duration.of(requestedPolicy.duration(), requestedPolicy.unit()).toDays();
            }
        } catch (ArithmeticException | IllegalArgumentException ex) {
            daysRequested = Long.MAX_VALUE;
        }

        if (daysRequested > MAX_DAYS) {
            return new RetentionPolicy(MAX_DAYS, ChronoUnit.DAYS, Instant.now());
        }

        if (daysRequested < MIN_DAYS) {
            return new RetentionPolicy(MIN_DAYS, ChronoUnit.DAYS, Instant.now());
        }
        return new RetentionPolicy(requestedPolicy.duration(), requestedPolicy.unit(), Instant.now());
    }
}
