package com.langa.backend.infra.persistence.repositories.teams;

import com.langa.backend.domain.teams.repositories.TeamMemberRepository;
import com.langa.backend.domain.teams.valueobjects.TeamMember;
import com.langa.backend.infra.persistence.repositories.teams.mongo.MongoTeamMemberDao;
import com.langa.backend.infra.persistence.repositories.teams.mongo.TeamMemberDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TeamMemberRepositoryImpl implements TeamMemberRepository {

    private final MongoTeamMemberDao mongoTeamMemberDao;

    @Override
    public TeamMember save(TeamMember teamMember) {
        return findByEmailAndTeamKey(teamMember.email(), teamMember.teamKey())
                .orElse(mongoTeamMemberDao.save(TeamMemberDocument.of(teamMember)).toTeamMember());
    }

    @Override
    public Optional<TeamMember> findByEmailAndTeamKey(String email, String teamKey) {
        return mongoTeamMemberDao.findByEmailAndTeamKey(email, teamKey)
                .map(TeamMemberDocument::toTeamMember);
    }

    @Override
    public List<TeamMember> findByEmail(String email) {
        return mongoTeamMemberDao.findByEmail(email)
                .stream()
                .map(TeamMemberDocument::toTeamMember)
                .toList();
    }

    @Override
    public void saveAll(List<TeamMember> teamMembers) {
        teamMembers.forEach(this::save);
    }
}
