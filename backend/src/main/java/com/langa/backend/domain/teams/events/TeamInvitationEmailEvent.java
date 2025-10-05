package com.langa.backend.domain.teams.events;

import com.langa.backend.common.eda.annotations.DomainEventType;
import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.registry.EventTypeRegistry;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.TeamInvitation;

import java.time.LocalDateTime;

@DomainEventType("TeamInvitationEmailEvent")
public record TeamInvitationEmailEvent(
        String aggregateId,
        String guest,
        String host,
        String team,
        String invitationToken,
        LocalDateTime expiration) implements DomainEvent {


    @Override
    public EventTypeRegistry getEventType() {
        return EventTypeRegistry.TEAM_INVITATION_EMAIL;
    }

    @Override
    public String getAggregateType() {
        return "TeamInvitation";
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    public static TeamInvitationEmailEvent of(TeamInvitation invitation, Team team) {
        return new TeamInvitationEmailEvent(
                invitation.getIdentity().id(),
                invitation.getStakeHolders().guest(),
                invitation.getStakeHolders().host(),
                team.getName(),
                invitation.getIdentity().invitationToken(),
                invitation.getInvitationPeriod().expiryDate()
        );
    }
}
