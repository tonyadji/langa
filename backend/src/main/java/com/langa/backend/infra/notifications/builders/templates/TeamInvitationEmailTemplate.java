package com.langa.backend.infra.notifications.builders.templates;

import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.registry.EventTypeRegistry;
import com.langa.backend.domain.teams.events.TeamInvitationEmailEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class TeamInvitationEmailTemplate extends EmailTemplate {

    private static final String TEAM_KEY = "teamKey";
    private static final String TEAM_NAME = "teamName";
    private static final String TOKEN_KEY = "invitationToken";
    private static final String RECIPIENTS_KEY = "recipients";
    private final String baseUrl;

    public TeamInvitationEmailTemplate(@Value("${application.front-url}") String baseUrl) {
        super();
        this.baseUrl = baseUrl;
    }

    @Override
    public String getSubject() {
        return "TEAM INVITATION";
    }

    @Override
    public String getMessage() {
        return "You have been invited to join the team " +variables.get(TEAM_NAME).toString() +
                "\nPlease follow the link "+baseUrl+
                "/team-invitations/"+variables.get(TEAM_KEY)+"/public?token="+variables.get(TOKEN_KEY).toString()+" to join the team";
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
            variables.put(TEAM_NAME, teamInvitationEmailEvent.teamName());
            variables.put(TEAM_KEY, teamInvitationEmailEvent.teamKey());
            variables.put(TOKEN_KEY, teamInvitationEmailEvent.invitationToken());
        }
    }
}
