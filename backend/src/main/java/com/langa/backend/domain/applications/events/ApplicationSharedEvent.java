package com.langa.backend.domain.applications.events;

import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.registry.EventTypeRegistry;

public record ApplicationSharedEvent(
        String aggregateId,
        String sharerEmail,
        String sharedWithEmail,
        String appName
) implements DomainEvent {
    @Override
    public EventTypeRegistry getEventType() {
        return EventTypeRegistry.APPLICATION_SHARED_EVENT;
    }

    @Override
    public String getAggregateType() {
        return "Application";
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }
}
