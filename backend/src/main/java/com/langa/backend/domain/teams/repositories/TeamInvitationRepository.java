package com.langa.backend.domain.teams.repositories;

import com.langa.backend.domain.teams.TeamInvitation;

import java.util.Optional;

public interface TeamInvitationRepository {

    TeamInvitation save(TeamInvitation teamInvitation);

    Optional<TeamInvitation> findExistingValidInvitation(String key, String guest);

    Optional<TeamInvitation> findByToken(String token);
}
