package com.langa.backend.infra.rest.teams.dto;

import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.valueobjects.TeamInvitation;

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

    public static List<TeamResponseDto> of(List<Team> teams) {
        return teams.stream().map(TeamResponseDto::ofLite).toList();
    }

    public static TeamResponseDto ofLite(Team team) {
        return new TeamResponseDto(team.getId(), team.getName(), team.getKey(), null, null, team.getCreatedBy(), null);
    }
}
