package com.langa.backend.domain.users.events;

import com.langa.backend.common.eda.annotations.DomainEventType;
import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.registry.EventTypeRegistry;
import com.langa.backend.domain.users.User;

@DomainEventType("FirstConnectionMailEvent")
public record FirstConnectionMailEvent(
        String aggregateId,
        String email,
        String firstConnectionToken
) implements DomainEvent {
    @Override
    public EventTypeRegistry getEventType() {
        return EventTypeRegistry.FIRST_CONNECTION_MAIL;
    }

    @Override
    public String getAggregateType() {
        return "User";
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    public static FirstConnectionMailEvent of(User user) {
        return new FirstConnectionMailEvent(user.getId(), user.getEmail(), "firstConnectionToken");
    }
}
