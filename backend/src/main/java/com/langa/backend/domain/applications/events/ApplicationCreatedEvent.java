package com.langa.backend.domain.applications.events;

import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.registry.EventTypeRegistry;
import com.langa.backend.domain.applications.Application;

public record ApplicationCreatedEvent(
        String aggregateId,
        String ownerEmail,
        String appName
) implements DomainEvent {
    @Override
    public EventTypeRegistry getEventType() {
        return EventTypeRegistry.APPLICATION_CREATED_EVENT;
    }

    @Override
    public String getAggregateType() {
        return "Application";
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    public static ApplicationCreatedEvent of(Application application) {
        return new ApplicationCreatedEvent(application.getId(), application.getOwner(), application.getName());
    }
}
