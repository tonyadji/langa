package com.langa.backend.domain.teams;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.valueobjects.InvitationStatus;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationIdentity;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationPeriod;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationStakeHolders;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class TeamInvitation {
    private final TeamInvitationIdentity identity;
    private final TeamInvitationStakeHolders stakeHolders;
    private final TeamInvitationPeriod invitationPeriod;
    private LocalDateTime acceptedDate;
    private InvitationStatus status;

    private TeamInvitation(TeamInvitationIdentity id, TeamInvitationStakeHolders stakeHolders, TeamInvitationPeriod invitationPeriod, LocalDateTime acceptedDate, InvitationStatus status) {
        this.identity = id;
        this.stakeHolders = stakeHolders;
        this.invitationPeriod = invitationPeriod;
        this.acceptedDate = acceptedDate;
        this.status = status;
    }

    public static TeamInvitation populate(TeamInvitationIdentity id, TeamInvitationStakeHolders stakeHolders, TeamInvitationPeriod invitationPeriod, LocalDateTime acceptedDate, InvitationStatus status) {
        return new TeamInvitation(id, stakeHolders, invitationPeriod, acceptedDate, status);
    }

    public TeamInvitation accept() {
        if(List.of(InvitationStatus.ACCEPTED, InvitationStatus.EXPIRED).contains(status)) {
            throw new TeamException("Invalid status", null, Errors.TEAM_INVITATION_INVALID_STATUS);
        }
        status = InvitationStatus.ACCEPTED;
        acceptedDate = LocalDateTime.now();
        return this;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(invitationPeriod.expiryDate());
    }

    public void markAsExpired() {
        status = InvitationStatus.EXPIRED;
    }
}
