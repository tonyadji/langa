package com.langa.backend.infra.rest.teams.dto;

import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.domain.teams.valueobjects.InvitationStatus;

import java.time.LocalDateTime;

public record GetInvitationResponseDto(
        String id,
        String team,
        String host,
        String guest,
        LocalDateTime epiryDate,
        InvitationStatus status
) {
    public static GetInvitationResponseDto of(TeamInvitation teamInvitation) {
        return new GetInvitationResponseDto(
                teamInvitation.getId(),
                teamInvitation.getTeam(),
                teamInvitation.getHost(),
                teamInvitation.getGuest(),
                teamInvitation.getExpiryDate(),
                teamInvitation.getStatus()
        );
    }
}
