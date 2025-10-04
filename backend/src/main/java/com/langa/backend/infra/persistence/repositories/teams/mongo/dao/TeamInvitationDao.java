package com.langa.backend.infra.persistence.repositories.teams.mongo.dao;

import com.langa.backend.infra.persistence.repositories.teams.mongo.documents.TeamInvitationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TeamInvitationDao extends MongoRepository<TeamInvitationDocument, String> {
    Optional<TeamInvitationDocument> findByTeamAndGuest(String key, String guest);

    Optional<TeamInvitationDocument> findByInvitationToken(String token);
}
