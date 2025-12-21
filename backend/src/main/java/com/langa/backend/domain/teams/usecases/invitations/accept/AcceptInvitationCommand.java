package com.langa.backend.domain.teams.usecases.invitations.accept;

import com.langa.backend.common.commands.Command;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.valueobjects.TeamInvitation;
import com.langa.backend.domain.teams.exceptions.TeamException;

public record AcceptInvitationCommand(String teamId, String invitationToken, String guest)
 implements Command<TeamInvitation> {

    public AcceptInvitationCommand {
        if (teamId == null || teamId.isBlank()) {
            throw new TeamException("teamId is required", null, Errors.VALIDATION_ERROR);
        }

        if (invitationToken == null || invitationToken.isBlank()) {
            throw new TeamException("invitationToken is required", null, Errors.VALIDATION_ERROR);
        }

        if (guest == null || guest.isBlank()) {
            throw new TeamException("guest is required", null, Errors.VALIDATION_ERROR);
        }
    }
}
