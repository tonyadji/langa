package com.langa.backend.domain.teams.usecases.invitations.fetch;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.exceptions.TeamException;

public record GetPublicInvitationQuery(
        String teamId,
        String invitationToken
) {

    public GetPublicInvitationQuery {
        if (teamId == null || teamId.isBlank()) {
            throw new TeamException("teamId is required", null, Errors.VALIDATION_ERROR);
        }
        if (invitationToken == null || invitationToken.isBlank()) {
            throw new TeamException("invitationToken is required", null, Errors.VALIDATION_ERROR);
        }
    }
}
