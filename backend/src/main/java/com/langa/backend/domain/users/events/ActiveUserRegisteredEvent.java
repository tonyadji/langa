package com.langa.backend.domain.users.events;

import com.langa.backend.common.eda.annotations.DomainEventType;
import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.registry.EventTypeRegistry;
import com.langa.backend.domain.users.User;

@DomainEventType("ActiveUserRegisteredEvent")
public record ActiveUserRegisteredEvent(
        String aggregateId,
        String email
) implements DomainEvent {
    @Override
    public EventTypeRegistry getEventType() {
        return EventTypeRegistry.ACTIVE_USER_REGISTERED;
    }

    @Override
    public String getAggregateType() {
        return "User";
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    public static ActiveUserRegisteredEvent of(User user) {
        return new ActiveUserRegisteredEvent(user.getId(), user.getEmail());
    }
}
