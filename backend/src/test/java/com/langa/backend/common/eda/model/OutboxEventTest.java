package com.langa.backend.common.eda.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutboxEventTest {

    @Test
    void createNew_shouldInitializeUnprocessedEvent() {
        OutboxEvent event = OutboxEvent.createNew("Application", "app-1", "APPLICATION_CREATED_EVENT", "{}");

        assertEquals("Application", event.getAggregateType());
        assertEquals("app-1", event.getAggregateId());
        assertEquals("APPLICATION_CREATED_EVENT", event.getEventType());
        assertEquals("{}", event.getPayload());
        assertFalse(event.isProcessed());
        assertFalse(event.isError());
        assertNotNull(event.getCreatedDate());
    }
}
