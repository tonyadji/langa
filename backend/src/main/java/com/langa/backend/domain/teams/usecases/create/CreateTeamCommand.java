package com.langa.backend.domain.teams.usecases.create;

import com.langa.backend.common.commands.Command;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.exceptions.TeamException;

public record CreateTeamCommand(
        String name, String ownerEmail
) implements Command<Team> {

    public CreateTeamCommand {
        if (name == null || name.isEmpty()) {
            throw new TeamException("Team name is required",  null, Errors.VALIDATION_ERROR);
        }

        if (ownerEmail == null || ownerEmail.isEmpty()) {
            throw new TeamException("Owner email is required",  null, Errors.VALIDATION_ERROR);
        }
    }
}
