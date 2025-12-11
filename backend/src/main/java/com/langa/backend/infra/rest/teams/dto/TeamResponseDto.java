package com.langa.backend.infra.rest.teams.dto;

import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.TeamInvitation;

import java.time.LocalDateTime;
import java.util.List;

public record TeamResponseDto(
        String id,
        String name,
        String key,
        List<TeamMemberDto> members,
        List<TeamInvitation> invitations,
        String createdBy,
        LocalDateTime createdDate
) {

    public static TeamResponseDto of(Team team) {
        List<TeamMemberDto> members = team.getMembers()
                .stream().map(TeamMemberDto::of).toList();
        return new TeamResponseDto(
                team.getId(),
                team.getName(),
                team.getKey(),
                members,
                team.getInvitations(),
                team.getCreatedBy(),
                team.getCreatedDate()
        );
    }
}
