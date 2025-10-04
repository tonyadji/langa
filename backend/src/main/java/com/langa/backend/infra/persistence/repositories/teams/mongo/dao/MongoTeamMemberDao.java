package com.langa.backend.infra.persistence.repositories.teams.mongo.dao;

import com.langa.backend.infra.persistence.repositories.teams.mongo.documents.TeamMemberDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MongoTeamMemberDao extends MongoRepository<TeamMemberDocument, String> {
    Optional<TeamMemberDocument> findByEmailAndTeamKey(String email, String teamKey);

    List<TeamMemberDocument> findByEmail(String email);
}
