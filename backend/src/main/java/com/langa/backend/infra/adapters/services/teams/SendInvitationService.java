package com.langa.backend.infra.adapters.services.teams;

import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.domain.teams.usecases.SendInvitationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SendInvitationService {

    private final SendInvitationUseCase sendInvitationUseCase;

    @Transactional
    public TeamInvitation invite(TeamInvitation teamInvitation) {
        return sendInvitationUseCase.invite(teamInvitation);
    }
}
