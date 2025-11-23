package com.langa.backend.infra.rest.teams.dto;

import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationStakeHolders;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteMemberRequestDto(
        @NotNull @NotBlank @Email String guest,
        @NotNull @NotBlank String team
) {

    public TeamInvitation toTeamInvitation(String host) {
        return TeamInvitation.populate(null, new TeamInvitationStakeHolders(team, host, guest),
                null, null, null);
    }
}
