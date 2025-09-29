package com.langa.backend.domain.teams.repositories;

import com.langa.backend.domain.teams.valueobjects.TeamMember;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository {

    TeamMember save(TeamMember teamMember);

    Optional<TeamMember> findByEmailAndTeamKey(String email, String teamKey);

    List<TeamMember> findByEmail(String owner);

    void saveAll(List<TeamMember> teamMembers);
}
