package com.langa.backend.infra.services.teams;

import com.langa.backend.common.model.ShareWithInfo;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamRepository;
import com.langa.backend.domainexchange.teams.TeamService;
import org.springframework.stereotype.Service;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    public TeamServiceImpl(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public String getTeamKey(String teamKey) {
        return teamRepository.findByKey(teamKey)
                .map(Team::getKey)
                .orElseThrow(() -> new TeamException("Team not found with key :"+teamKey, null, Errors.TEAM_NOT_FOUND));
    }

    @Override
    public ShareWithInfo getShareWithInfo(String teamKey) {
        return teamRepository.findByKey(teamKey)
                .map(team -> new ShareWithInfo(team.getKey(), team.getCreatedBy()))
                .orElseThrow(() -> new TeamException("Team not found with key :"+teamKey, null, Errors.TEAM_NOT_FOUND));
    }
}
