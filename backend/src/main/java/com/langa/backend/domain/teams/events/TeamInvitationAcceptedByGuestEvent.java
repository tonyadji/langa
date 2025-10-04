package com.langa.backend.domain.teams.events;

import com.langa.backend.common.eda.annotations.DomainEventType;
import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.registry.EventTypeRegistry;
import com.langa.backend.domain.teams.TeamInvitation;

import java.time.LocalDateTime;

@DomainEventType("TeamInvitationAcceptedByGuestEvent")
public record TeamInvitationAcceptedByGuestEvent(
        String aggregateId,
        String guest,
        String host,
        String team,
        String invitationToken,
        LocalDateTime expiration) implements DomainEvent {


    @Override
    public EventTypeRegistry getEventType() {
        return EventTypeRegistry.TEAM_INVITATION_EMAIL_ACCEPTED_BY_GUEST;
    }

    @Override
    public String getAggregateType() {
        return "TeamInvitation";
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    public static TeamInvitationAcceptedByGuestEvent of(TeamInvitation invitation) {
        return new TeamInvitationAcceptedByGuestEvent(
                invitation.getId(),
                invitation.getGuest(),
                invitation.getHost(),
                invitation.getTeam(),
                invitation.getInvitationToken(),
                invitation.getExpiryDate()
        );
    }
}
