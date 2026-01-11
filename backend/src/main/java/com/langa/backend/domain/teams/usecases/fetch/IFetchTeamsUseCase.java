package com.langa.backend.domain.teams.usecases.fetch;

import com.langa.backend.domain.teams.Team;

import java.util.List;

public interface IFetchTeamsUseCase {

    Team query(GetTeamQuery query);

    List<Team> queryTeams(String owner);
}
