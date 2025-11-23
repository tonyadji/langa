package com.langa.backend.domain.teams.repositories;

import com.langa.backend.domain.teams.Team;

import java.util.List;
import java.util.Optional;

public interface TeamRepository {

    Team save(Team team);

    Optional<Team> findByOwnerAndName(String owner, String name);

    Optional<Team> findByKey(String key);

    Optional<Team> findById(String id);

    List<Team> findByOwner(String owner);
}
