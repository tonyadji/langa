package com.langa.backend.domain.users.events;

import com.langa.backend.common.eda.annotations.DomainEventType;
import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.registry.EventTypeRegistry;
import com.langa.backend.domain.users.User;

@DomainEventType("AccountSetupCompleteMailEvent")
public record AccountSetupCompleteMailEvent(
        String aggregateId,
        String email
) implements DomainEvent {
    @Override
    public EventTypeRegistry getEventType() {
        return EventTypeRegistry.ACCOUNT_SETUP_COMPLETE_MAIL;
    }

    @Override
    public String getAggregateType() {
        return "User";
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    public static AccountSetupCompleteMailEvent of(User user) {
        return new AccountSetupCompleteMailEvent(user.getUserId().id(), user.getUserId().email());
    }
}
