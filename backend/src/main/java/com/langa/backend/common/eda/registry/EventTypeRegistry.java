package com.langa.backend.common.eda.registry;

import com.langa.backend.common.eda.model.OutboxEvent;
import com.langa.backend.domain.teams.events.TeamInvitationEmailEvent;
import lombok.Getter;

@Getter
public enum EventTypeRegistry {

    OUTBOX_EVENT(OutboxEvent.class),
    TEAM_INVITATION_EMAIL(TeamInvitationEmailEvent.class);

    private final Class<?> eventClass;

    EventTypeRegistry(Class<?> eventType) {
        this.eventClass = eventType;
    }
}
