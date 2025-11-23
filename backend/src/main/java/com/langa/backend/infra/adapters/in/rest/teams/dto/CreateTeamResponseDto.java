package com.langa.backend.infra.adapters.in.rest.teams.dto;

import com.langa.backend.domain.teams.Team;

import java.time.LocalDateTime;
import java.util.List;

public record CreateTeamResponseDto(
        String id,
        String name,
        String key,
        List<TeamMemberDto> members,
        String createdBy,
        LocalDateTime createdDate
) {

    public static CreateTeamResponseDto of(Team team) {
        List<TeamMemberDto> members = team.getMembers()
                .stream().map(TeamMemberDto::of).toList();
        return new CreateTeamResponseDto(
                team.getId(),
                team.getName(),
                team.getKey(),
                members,
                team.getCreatedBy(),
                team.getCreatedDate()
        );
    }
}
