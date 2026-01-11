package com.langa.backend.domain.teams.usecases.fetch;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamRepository;

import java.util.List;

@UseCase
public class GetTeamsUseCase implements IFetchTeamsUseCase {

    private final TeamRepository teamRepository;

    public GetTeamsUseCase(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public Team query(GetTeamQuery query) {
        final Team team = teamRepository.findById(query.teamId()).orElse(null);

        if (team == null) {
            throw new TeamException("Team not found with id : "+query.teamId(), null, Errors.TEAM_NOT_FOUND);
        }
        team.checkOwnership(query.owner());
        return team;
    }

    @Override
    public List<Team> queryTeams(String owner) {
        return teamRepository.findByOwnerOrTeamMember(owner);
    }
}
