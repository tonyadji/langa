package com.langa.backend.infra.rest.teams.dto;

import com.langa.backend.domain.teams.TeamInvitation;

import java.time.LocalDateTime;

public record InviteMemberResponseDto(
        String guest,
        String team,
        String invitationToken,
        LocalDateTime expiration
) {
    public static InviteMemberResponseDto of(TeamInvitation invitation) {
        return new InviteMemberResponseDto(
                invitation.getGuest(),
                invitation.getTeam(),
                invitation.getInvitationToken(),
                invitation.getExpiryDate()
        );
    }
}
