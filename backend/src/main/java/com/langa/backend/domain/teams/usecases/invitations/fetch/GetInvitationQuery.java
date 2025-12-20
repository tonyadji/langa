package com.langa.backend.domain.teams.usecases.invitations.fetch;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.exceptions.TeamException;

public record GetInvitationQuery(
        String teamId,
        String invitationToken,
        String guestOrHost
) {

    public GetInvitationQuery {
        if (teamId == null || teamId.isBlank()) {
            throw new TeamException("teamId is required", null, Errors.VALIDATION_ERROR);
        }

        if (invitationToken == null || invitationToken.isBlank()) {
            throw new TeamException("invitationToken is required", null, Errors.VALIDATION_ERROR);
        }

        if (guestOrHost == null || guestOrHost.isBlank()) {
            throw new TeamException("guest Or Host is required", null, Errors.VALIDATION_ERROR);
        }
    }
}
