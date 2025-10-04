package com.langa.backend.domain.teams;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.valueobjects.InvitationStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain=true)
public class TeamInvitation {
    private String id;
    private String team;
    private String host;
    private String guest;
    private String invitationToken;
    private LocalDateTime inviteDate;
    private LocalDateTime expiryDate;
    private LocalDateTime acceptedDate;
    private InvitationStatus status;

    public TeamInvitation checkExpiration() {
        if (LocalDateTime.now().isAfter(expiryDate)) {
            this.status = InvitationStatus.EXPIRED;
        }
        return this;
    }

    public TeamInvitation couldBeAccepted() {
        if(List.of(InvitationStatus.ACCEPTED, InvitationStatus.EXPIRED).contains(status)) {
            throw new TeamException("Invalid status", null, Errors.TEAM_INVITATION_INVALID_STATUS);
        }
        status = InvitationStatus.ACCEPTED;
        acceptedDate = LocalDateTime.now();
        return this;
    }
}
