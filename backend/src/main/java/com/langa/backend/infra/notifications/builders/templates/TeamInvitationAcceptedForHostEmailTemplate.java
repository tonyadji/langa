package com.langa.backend.infra.notifications.builders.templates;

import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.registry.EventTypeRegistry;
import com.langa.backend.domain.teams.events.TeamInvitationAcceptedForHostEvent;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class TeamInvitationAcceptedForHostEmailTemplate extends EmailTemplate {

    private static final String TEAM_KEY = "team";
    private static final String GUEST_KEY = "guest";
    private static final String RECIPIENTS_KEY = "recipients";

    public TeamInvitationAcceptedForHostEmailTemplate() {
        super();
    }

    @Override
    public String getSubject() {
        return "INVITATION ACCEPTED";
    }

    @Override
    public String getMessage() {
        return "The invitation you sent to " +variables.get(GUEST_KEY).toString() +
                "for the team " + variables.get(TEAM_KEY).toString() + "has been accepted.";
    }

    @Override
    public List<String> getRecipients() {
        return (List<String>) variables.get(RECIPIENTS_KEY);
    }

    @Override
    public boolean couldProcess(DomainEvent event) {
        return EventTypeRegistry.TEAM_INVITATION_EMAIL_ACCEPTED_FOR_HOST.equals(event.getEventType());
    }

    @Override
    public void processEvent(DomainEvent event) {
        if(event instanceof TeamInvitationAcceptedForHostEvent invitationAcceptedForHostEvent) {
            variables.put(RECIPIENTS_KEY, List.of(invitationAcceptedForHostEvent.host()));
            variables.put(TEAM_KEY, invitationAcceptedForHostEvent.team());
            variables.put(GUEST_KEY, invitationAcceptedForHostEvent.guest());
        }
    }
}
