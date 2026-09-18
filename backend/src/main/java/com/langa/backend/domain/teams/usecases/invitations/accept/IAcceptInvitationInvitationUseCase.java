package com.langa.backend.domain.teams.usecases.invitations.accept;

import com.langa.backend.domain.teams.valueobjects.TeamInvitation;

public interface IAcceptInvitationInvitationUseCase {

    TeamInvitation execute(AcceptInvitationCommand command);
}
