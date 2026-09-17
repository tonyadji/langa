package com.langa.backend.domain.applications.valueobjects;

import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class RetentionPolicyTest {

    @Test
    void defaultPolicy_shouldBe45Days() {
        RetentionPolicy policy = RetentionPolicy.defaultPolicy();
        assertEquals(45, policy.duration());
        assertEquals(ChronoUnit.DAYS, policy.unit());
    }

    @Test
    void update_shouldReturnSameInstance_whenRequestedIsNull() {
        RetentionPolicy policy = RetentionPolicy.defaultPolicy();
        assertSame(policy, policy.update(null));
    }

    @Test
    void update_shouldApplyRequestedPolicy_whenWithinBounds() {
        RetentionPolicy policy = RetentionPolicy.defaultPolicy();
        RetentionPolicy updated = policy.update(new RetentionPolicy(10, ChronoUnit.DAYS, null));

        assertEquals(10, updated.duration());
        assertEquals(ChronoUnit.DAYS, updated.unit());
        assertNotNull(updated.lastUpdatedDate());
    }

    @Test
    void update_shouldClampToMax_whenRequestedExceedsMax() {
        RetentionPolicy policy = RetentionPolicy.defaultPolicy();
        RetentionPolicy updated = policy.update(new RetentionPolicy(100, ChronoUnit.DAYS, null));

        assertEquals(45, updated.duration());
        assertEquals(ChronoUnit.DAYS, updated.unit());
    }

    @Test
    void update_shouldClampToMin_whenRequestedBelowMin() {
        RetentionPolicy policy = RetentionPolicy.defaultPolicy();
        RetentionPolicy updated = policy.update(new RetentionPolicy(0, ChronoUnit.DAYS, null));

        assertEquals(1, updated.duration());
        assertEquals(ChronoUnit.DAYS, updated.unit());
    }
}
