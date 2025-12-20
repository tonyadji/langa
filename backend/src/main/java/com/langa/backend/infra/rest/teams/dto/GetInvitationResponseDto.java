package com.langa.backend.infra.rest.teams.dto;

import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.domain.teams.valueobjects.InvitationStatus;

import java.time.LocalDateTime;

public record GetInvitationResponseDto(
        String id,
        String token,
        String team,
        String host,
        String guest,
        LocalDateTime expiryDate,
        InvitationStatus status
) {
    public static GetInvitationResponseDto of(TeamInvitation teamInvitation) {
        return new GetInvitationResponseDto(
                teamInvitation.getTeamId(),
                teamInvitation.getToken(),
                teamInvitation.getStakeHolders().team(),
                teamInvitation.getStakeHolders().host(),
                teamInvitation.getStakeHolders().guest(),
                teamInvitation.getInvitationPeriod().expiryDate(),
                teamInvitation.getStatus()
        );
    }
}
