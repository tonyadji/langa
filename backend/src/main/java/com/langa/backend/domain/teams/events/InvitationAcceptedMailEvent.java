package com.langa.backend.domain.teams.events;

import com.langa.backend.common.eda.annotations.DomainEventType;
import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.registry.EventTypeRegistry;
import com.langa.backend.domain.teams.Team;

@DomainEventType("InvitationAcceptedMailEvent")
public record InvitationAcceptedMailEvent(
        String aggregateId,
        String team,
        String member
) implements DomainEvent {
    @Override
    public EventTypeRegistry getEventType() {
        return EventTypeRegistry.INVITATION_ACCEPTED_EMAIL;
    }

    @Override
    public String getAggregateType() {
        return "Team";
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    public static InvitationAcceptedMailEvent of(Team team, String member) {
        return new InvitationAcceptedMailEvent(team.getId(), team.getName(), member);
    }
}
