package com.langa.backend.domain.teams.usecases.invitations.send;

import com.langa.backend.common.commands.Command;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.exceptions.TeamException;

public record SendInvitationCommand(
    String guest,
    String team,
    String host
) implements Command<Team> {

    public SendInvitationCommand {
        if (guest == null || guest.isBlank()) {
            throw new TeamException("Guest email is required", null, Errors.VALIDATION_ERROR);
        }

        if (team == null || team.isBlank()) {
            throw new TeamException("Team id is required", null, Errors.VALIDATION_ERROR);
        }

        if (host == null || host.isBlank()) {
            throw new TeamException("Host email is required", null, Errors.VALIDATION_ERROR);
        }
    }
}
