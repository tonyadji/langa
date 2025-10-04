package com.langa.backend.infra.notifications.builders.templates;

import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.registry.EventTypeRegistry;
import com.langa.backend.domain.teams.events.TeamInvitationEmailEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class TeamInvitationEmailTemplate extends EmailTemplate {

    private static final String TEAM_KEY = "team";
    private static final String TOKEN_KEY = "invitationToken";
    private static final String RECIPIENTS_KEY = "recipients";
    private final String baseUrl;

    public TeamInvitationEmailTemplate(@Value("${application.base-url}") String baseUrl) {
        super();
        this.baseUrl = baseUrl;
    }

    @Override
    public String getSubject() {
        return "TEAM INVITATION";
    }

    @Override
    public String getMessage() {
        return "You have been invited to join a team" +
                "Please follow the link "+baseUrl+
                "/teams/"+variables.get(TEAM_KEY).toString()+"/invitations?token="+variables.get(TOKEN_KEY).toString()+" to join the team";
    }

    @Override
    public List<String> getRecipients() {
        return (List<String>) variables.get(RECIPIENTS_KEY);
    }

    @Override
    public boolean couldProcess(DomainEvent event) {
        return EventTypeRegistry.TEAM_INVITATION_EMAIL.equals(event.getEventType());
    }

    @Override
    public void processEvent(DomainEvent event) {
        if(event instanceof TeamInvitationEmailEvent teamInvitationEmailEvent) {
            variables.put(RECIPIENTS_KEY, List.of(teamInvitationEmailEvent.guest()));
            variables.put(TEAM_KEY, teamInvitationEmailEvent.team());
            variables.put(TOKEN_KEY, teamInvitationEmailEvent.invitationToken());
        }
    }
}
