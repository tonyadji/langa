package com.langa.backend.infra.rest.teams.dto;

import com.langa.backend.domain.teams.TeamInvitation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteMemberRequestDto(
        @NotNull @NotBlank @Email String guest,
        @NotNull @NotBlank String team
) {

    public TeamInvitation toTeamInvitation(String host) {
        return new TeamInvitation()
                .setGuest(guest)
                .setTeam(team)
                .setHost(host);
    }
}
