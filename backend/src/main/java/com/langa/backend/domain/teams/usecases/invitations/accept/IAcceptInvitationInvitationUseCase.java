package com.langa.backend.domain.teams.usecases.invitations.accept;

import com.langa.backend.domain.teams.TeamInvitation;

public interface IAcceptInvitationInvitationUseCase {

    TeamInvitation execute(AcceptInvitationCommand command);
}
