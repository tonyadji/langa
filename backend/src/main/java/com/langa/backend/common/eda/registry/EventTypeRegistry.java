package com.langa.backend.common.eda.registry;

import com.langa.backend.common.eda.model.OutboxEvent;
import com.langa.backend.domain.teams.events.TeamInvitationAcceptedByGuestEvent;
import com.langa.backend.domain.teams.events.TeamInvitationAcceptedForHostEvent;
import com.langa.backend.domain.teams.events.TeamInvitationEmailEvent;
import lombok.Getter;

@Getter
public enum EventTypeRegistry {

    OUTBOX_EVENT(OutboxEvent.class),
    TEAM_INVITATION_EMAIL(TeamInvitationEmailEvent.class),
    TEAM_INVITATION_EMAIL_ACCEPTED_FOR_HOST(TeamInvitationAcceptedForHostEvent.class),
    TEAM_INVITATION_EMAIL_ACCEPTED_BY_GUEST(TeamInvitationAcceptedByGuestEvent.class);

    private final Class<?> eventClass;

    EventTypeRegistry(Class<?> eventType) {
        this.eventClass = eventType;
    }
}
