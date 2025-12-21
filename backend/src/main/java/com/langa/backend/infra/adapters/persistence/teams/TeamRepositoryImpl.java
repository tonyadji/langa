package com.langa.backend.infra.adapters.persistence.teams;

import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.repositories.TeamRepository;
import com.langa.backend.infra.adapters.persistence.teams.mongo.dao.MongoTeamDao;
import com.langa.backend.infra.adapters.persistence.teams.mongo.dao.MongoTeamMemberDao;
import com.langa.backend.infra.adapters.persistence.teams.mongo.documents.TeamDocument;
import com.langa.backend.infra.adapters.persistence.teams.mongo.documents.TeamMemberDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class TeamRepositoryImpl implements TeamRepository {

    private final MongoTeamDao mongoTeamDao;
    private final MongoTeamMemberDao mongoTeamMemberDao;

    @Override
    public Team save(Team team) {
        if (!team.getNewMembers().isEmpty()) {
            mongoTeamMemberDao.saveAll(
                    team.getNewMembers()
                            .stream()
                            .map(TeamMemberDocument::of)
                            .toList()
            );
            team.getNewMembers().clear();
        }
        return mongoTeamDao.save(TeamDocument.of(team))
                .toTeam();
    }

    @Override
    public Optional<Team> findByOwnerAndName(String owner, String name) {
        return mongoTeamDao.findByCreatedByAndName(owner, name)
                .map(TeamDocument::toTeam);
    }

    @Override
    public Optional<Team> findByKey(String key) {
        return mongoTeamDao.findByKey(key)
                .map(TeamDocument::toTeam);
    }

    @Override
    public Optional<Team> findById(String id) {
        return mongoTeamDao.findById(id)
                .map(TeamDocument::toTeam);
    }

    @Override
    public List<Team> findByOwner(String owner) {
        return mongoTeamDao.findByCreatedBy(owner)
                .stream()
                .map(TeamDocument::toTeam)
                .toList();
    }

    @Override
    public Set<String> findTeamsKeysByMemberUsername(String username) {
        return Set.of();
    }

    @Override
    public List<Team> findByOwnerOrTeamSharedWith(String owner) {
        List<Team> ownedTeams = mongoTeamDao.findByCreatedBy(owner)
                .stream()
                .map(TeamDocument::toTeam)
                .toList();
        List<String> teamMemberKeys = mongoTeamMemberDao.findByEmail(owner)
                .stream()
                .map(TeamMemberDocument::getTeamKey).toList();
        List<Team> sharedTeams = mongoTeamDao.findByKeyIn(teamMemberKeys)
                .stream().map(TeamDocument::toSharedTeam).toList();
        return Stream.of(ownedTeams, sharedTeams).flatMap(List::stream).toList();
    }
}
