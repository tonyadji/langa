package com.langa.backend.domain.teams.events;

import com.langa.backend.common.eda.annotations.DomainEventType;
import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.registry.EventTypeRegistry;
import com.langa.backend.domain.teams.TeamInvitation;

import java.time.LocalDateTime;

@DomainEventType("TeamInvitationAcceptedForHostEvent")
public record TeamInvitationAcceptedForHostEvent(
        String aggregateId,
        String guest,
        String host,
        String team,
        String invitationToken,
        LocalDateTime expiration) implements DomainEvent {


    @Override
    public EventTypeRegistry getEventType() {
        return EventTypeRegistry.TEAM_INVITATION_EMAIL_ACCEPTED_FOR_HOST;
    }

    @Override
    public String getAggregateType() {
        return "TeamInvitation";
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    public static TeamInvitationAcceptedForHostEvent of(TeamInvitation invitation) {
        return new TeamInvitationAcceptedForHostEvent(
                invitation.getId(),
                invitation.getGuest(),
                invitation.getHost(),
                invitation.getTeam(),
                invitation.getInvitationToken(),
                invitation.getExpiryDate()
        );
    }
}
