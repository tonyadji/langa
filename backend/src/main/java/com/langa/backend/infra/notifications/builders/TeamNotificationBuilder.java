package com.langa.backend.infra.notifications.builders;

import com.langa.backend.domain.teams.events.TeamInvitationEmailEvent;
import com.langa.backend.infra.notifications.model.DomainNotification;
import com.langa.backend.infra.notifications.model.EmailNotification;

import java.util.List;

public class TeamNotificationBuilder {

    private TeamNotificationBuilder() {
    }

    public static DomainNotification build(TeamInvitationEmailEvent teamInvitationEmailEvent) {
        return new EmailNotification()
                .setRecipients(List.of(teamInvitationEmailEvent.guest()))
                .setSubject("Team Invitation")
                .setBody("You have been invited to join the team " + teamInvitationEmailEvent.team() + "."+
                        "Please follow the link http://localhost:8080/teams/"+teamInvitationEmailEvent.team()+"/invitations/"+teamInvitationEmailEvent.invitationToken()+" to join the team");
    }
}
