package com.langa.backend.domain.teams.valueobjects;

public record TeamInvitationIdentity(
        String id,
        String invitationToken
) {
}
