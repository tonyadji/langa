package com.langa.backend.infra.services.teams;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamRepository;
import com.langa.backend.domain.teams.services.TeamMemberShipService;
import org.springframework.stereotype.Service;

@Service
public class TeamMembershipServiceImpl implements TeamMemberShipService {

    private final TeamRepository teamRepository;

    public TeamMembershipServiceImpl(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public Team addMemberToTeam(String teamKey, String memberEmail) {
        final Team team = teamRepository.findByKey(teamKey)
                .orElseThrow(() -> new TeamException("Team not found", null, Errors.TEAM_NOT_FOUND));

        team.addMember(memberEmail);

        teamRepository.save(team);
        return team;
    }
}
