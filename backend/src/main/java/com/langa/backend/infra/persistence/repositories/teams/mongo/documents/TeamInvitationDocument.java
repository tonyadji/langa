package com.langa.backend.infra.persistence.repositories.teams.mongo.documents;

import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.domain.teams.valueobjects.InvitationStatus;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "c_team_invitations")
@Data
@Accessors(chain = true)
public class TeamInvitationDocument {
    private String id;
    private String team;
    private String host;
    private String guest;
    private String invitationToken;
    private LocalDateTime inviteDate;
    private LocalDateTime expiryDate;
    private LocalDateTime acceptedDate;
    private InvitationStatus status;

    public TeamInvitation toTeamInvitation() {
        return new TeamInvitation()
                .setId(id)
                .setTeam(team)
                .setHost(host)
                .setGuest(guest)
                .setInvitationToken(invitationToken)
                .setInviteDate(inviteDate)
                .setExpiryDate(expiryDate)
                .setAcceptedDate(acceptedDate)
                .setStatus(status);
    }

    public static TeamInvitationDocument of(TeamInvitation teamInvitation) {
        TeamInvitationDocument teamInvitationDocument = new TeamInvitationDocument();
        teamInvitationDocument.setId(teamInvitation.getId());
        teamInvitationDocument.setTeam(teamInvitation.getTeam());
        teamInvitationDocument.setHost(teamInvitation.getHost());
        teamInvitationDocument.setGuest(teamInvitation.getGuest());
        teamInvitationDocument.setInvitationToken(teamInvitation.getInvitationToken());
        teamInvitationDocument.setInviteDate(teamInvitation.getInviteDate());
        teamInvitationDocument.setExpiryDate(teamInvitation.getExpiryDate());
        teamInvitationDocument.setAcceptedDate(teamInvitation.getAcceptedDate());
        teamInvitationDocument.setStatus(teamInvitation.getStatus());
        return teamInvitationDocument;
    }
}
