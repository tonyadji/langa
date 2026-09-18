package com.langa.backend.domain.teams.valueobjects;

import com.langa.backend.common.utils.KeyGenerator;

public record TeamInvitationIdentity(
        String teamId,
        String invitationToken
) {
    public static TeamInvitationIdentity of(TeamId teamId) {
        return new TeamInvitationIdentity(teamId.id(), KeyGenerator.genericToken());
    }
}
