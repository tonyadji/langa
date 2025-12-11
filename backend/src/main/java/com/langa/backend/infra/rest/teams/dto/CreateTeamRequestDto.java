package com.langa.backend.infra.rest.teams.dto;

import com.langa.backend.domain.teams.usecases.create.CreateTeamCommand;

public record CreateTeamRequestDto(String name) {

    public CreateTeamCommand toCommand(String owner) {
        return new CreateTeamCommand(name, owner);
    }
}
