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
import java.util.Objects;

@Getter
public class TeamInvitation {
    private final TeamInvitationIdentity identity;
    private final TeamInvitationStakeHolders stakeHolders;
    private final TeamInvitationPeriod invitationPeriod;
    private LocalDateTime acceptedDate;
    private InvitationStatus status;

    private TeamInvitation(TeamInvitationIdentity identity, TeamInvitationStakeHolders stakeHolders, TeamInvitationPeriod invitationPeriod, LocalDateTime acceptedDate, InvitationStatus status) {
        this.identity = identity;
        this.stakeHolders = stakeHolders;
        this.invitationPeriod = invitationPeriod;
        this.acceptedDate = acceptedDate;
        this.status = status;
    }

    public static TeamInvitation populate(TeamInvitationIdentity identity, TeamInvitationStakeHolders stakeHolders, TeamInvitationPeriod invitationPeriod, LocalDateTime acceptedDate, InvitationStatus status) {
        return new TeamInvitation(identity, stakeHolders, invitationPeriod, acceptedDate, status);
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

    public String getTeamId() {
        return identity.teamId();
    }

    public String getToken() {
        return identity.invitationToken();
    }

    public boolean isVisibleBy(String guestOrHost) {
        return Objects.equals(stakeHolders.guest(), guestOrHost) ||
                Objects.equals(stakeHolders.host(), guestOrHost);
    }

    public boolean canAccept(String guest) {
        return Objects.equals(stakeHolders.guest(), guest);
    }
}
