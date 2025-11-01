package com.langa.backend.infra.rest.teams.dto;

import com.langa.backend.domain.teams.valueobjects.TeamMember;
import com.langa.backend.domain.teams.valueobjects.TeamRole;

import java.time.LocalDateTime;

public record TeamMemberDto(
        String email,
        TeamRole role,
        LocalDateTime addedDate
) {
    public static TeamMemberDto of(TeamMember teamMember) {
        return new TeamMemberDto(teamMember.email(), teamMember.role(), teamMember.addedDate());
    }
}
