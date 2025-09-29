package com.langa.backend.infra.persistence.repositories.teams;

import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.repositories.TeamRepository;
import com.langa.backend.infra.persistence.repositories.teams.mongo.MongoTeamDao;
import com.langa.backend.infra.persistence.repositories.teams.mongo.TeamDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TeamRepositoryImpl implements TeamRepository {

    private final MongoTeamDao mongoTeamDao;

    @Override
    public Team save(Team team) {
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
}
