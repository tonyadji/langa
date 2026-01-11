package com.langa.backend.infra.rest.teams.dto;

import com.langa.backend.domain.teams.usecases.invitations.send.SendInvitationCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteMemberRequestDto(
        @NotNull @NotBlank @Email String guest,
        @NotNull @NotBlank String team
) {

    public SendInvitationCommand toCommand(String host) {
        return new SendInvitationCommand(guest, team, host);
    }
}
