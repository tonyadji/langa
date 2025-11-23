package com.langa.backend.infra.adapters.in.rest.teams.dto;

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
                invitation.getStakeHolders().guest(),
                invitation.getStakeHolders().team(),
                invitation.getIdentity().invitationToken(),
                invitation.getInvitationPeriod().expiryDate()
        );
    }
}
