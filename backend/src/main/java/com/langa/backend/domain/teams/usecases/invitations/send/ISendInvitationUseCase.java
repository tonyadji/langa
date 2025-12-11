package com.langa.backend.domain.teams.usecases.invitations.send;

import com.langa.backend.domain.teams.Team;

public interface ISendInvitationUseCase {

    Team execute(SendInvitationCommand command);
}
