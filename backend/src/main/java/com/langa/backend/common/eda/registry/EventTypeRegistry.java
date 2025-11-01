package com.langa.backend.common.eda.registry;

import com.langa.backend.common.eda.model.OutboxEvent;
import com.langa.backend.domain.applications.events.ApplicationSharedEvent;
import com.langa.backend.domain.teams.events.InvitationAcceptedMailEvent;
import com.langa.backend.domain.teams.events.TeamInvitationAcceptedByGuestEvent;
import com.langa.backend.domain.teams.events.TeamInvitationAcceptedForHostEvent;
import com.langa.backend.domain.teams.events.TeamInvitationEmailEvent;
import com.langa.backend.domain.users.events.AccountSetupCompleteMailEvent;
import com.langa.backend.domain.users.events.FirstConnectionMailEvent;
import lombok.Getter;

@Getter
public enum EventTypeRegistry {

    ACCOUNT_SETUP_COMPLETE_MAIL(AccountSetupCompleteMailEvent.class),
    APPLICATION_SHARED_EVENT(ApplicationSharedEvent.class),
    FIRST_CONNECTION_MAIL(FirstConnectionMailEvent.class),
    INVITATION_ACCEPTED_EMAIL(InvitationAcceptedMailEvent.class),
    OUTBOX_EVENT(OutboxEvent.class),
    TEAM_INVITATION_EMAIL(TeamInvitationEmailEvent.class),
    TEAM_INVITATION_EMAIL_ACCEPTED_FOR_HOST(TeamInvitationAcceptedForHostEvent.class),
    TEAM_INVITATION_EMAIL_ACCEPTED_BY_GUEST(TeamInvitationAcceptedByGuestEvent.class);

    private final Class<?> eventClass;

    EventTypeRegistry(Class<?> eventType) {
        this.eventClass = eventType;
    }
}
