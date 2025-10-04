package com.langa.backend.domain.teams.usecases;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamMemberRepository;
import com.langa.backend.domain.teams.repositories.TeamRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CreateTeamUseCase {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    public CreateTeamUseCase(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    public Team createTeam(String name, String ownerEmail) {
        if (teamRepository.findByOwnerAndName(ownerEmail, name).isPresent()) {
            throw new TeamException("Team name already exists", null, Errors.TEAM_NAME_ALREADY_EXISTS);
        }
        final Team team = teamRepository.save(Team.createNew(name, ownerEmail, LocalDateTime.now()));
        teamMemberRepository.saveAll(team.getMembers());
        return team;
    }
}
