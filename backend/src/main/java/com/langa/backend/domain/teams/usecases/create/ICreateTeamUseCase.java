package com.langa.backend.domain.teams.usecases.create;

import com.langa.backend.domain.teams.Team;

public interface ICreateTeamUseCase {

    Team execute(CreateTeamCommand command);
}
