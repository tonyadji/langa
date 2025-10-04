package com.langa.backend.infra.persistence.repositories.teams;

import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.domain.teams.repositories.TeamInvitationRepository;
import com.langa.backend.infra.persistence.repositories.teams.mongo.dao.TeamInvitationDao;
import com.langa.backend.infra.persistence.repositories.teams.mongo.documents.TeamInvitationDocument;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TeamInvitationRepositoryImpl implements TeamInvitationRepository {

    private final TeamInvitationDao teamInvitationDao;

    public TeamInvitationRepositoryImpl(TeamInvitationDao teamInvitationDao) {
        this.teamInvitationDao = teamInvitationDao;
    }

    @Override
    public TeamInvitation save(TeamInvitation teamInvitation) {
        return teamInvitationDao.save(TeamInvitationDocument.of(teamInvitation))
                .toTeamInvitation();
    }

    @Override
    public Optional<TeamInvitation> findExistingValidInvitation(String key, String guest) {
        return teamInvitationDao.findByTeamAndGuest(key, guest)
                .map(TeamInvitationDocument::toTeamInvitation);
    }

    @Override
    public Optional<TeamInvitation> findByToken(String token) {
        return teamInvitationDao.findByInvitationToken(token)
                .map(TeamInvitationDocument::toTeamInvitation);
    }
}
