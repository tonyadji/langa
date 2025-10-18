package com.langa.backend.infra.persistence.repositories.teams.mongo.documents;

import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.domain.teams.valueobjects.InvitationStatus;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationIdentity;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationPeriod;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationStakeHolders;
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
        return TeamInvitation.populate(
                new TeamInvitationIdentity(id, invitationToken),
                new TeamInvitationStakeHolders(team, host, guest),
                new TeamInvitationPeriod(inviteDate, expiryDate),
                acceptedDate, status);
    }

    public static TeamInvitationDocument of(TeamInvitation teamInvitation) {
        TeamInvitationDocument teamInvitationDocument = new TeamInvitationDocument();
        teamInvitationDocument.setId(teamInvitation.getIdentity().id());
        teamInvitationDocument.setTeam(teamInvitation.getStakeHolders().team());
        teamInvitationDocument.setHost(teamInvitation.getStakeHolders().host());
        teamInvitationDocument.setGuest(teamInvitation.getStakeHolders().guest());
        teamInvitationDocument.setInvitationToken(teamInvitation.getIdentity().invitationToken());
        teamInvitationDocument.setInviteDate(teamInvitation.getInvitationPeriod().inviteDate());
        teamInvitationDocument.setExpiryDate(teamInvitation.getInvitationPeriod().expiryDate());
        teamInvitationDocument.setAcceptedDate(teamInvitation.getAcceptedDate());
        teamInvitationDocument.setStatus(teamInvitation.getStatus());
        return teamInvitationDocument;
    }
}
