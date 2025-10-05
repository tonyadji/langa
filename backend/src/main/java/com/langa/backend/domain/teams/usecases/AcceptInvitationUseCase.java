package com.langa.backend.domain.teams.usecases;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.domain.teams.events.TeamInvitationAcceptedByGuestEvent;
import com.langa.backend.domain.teams.events.TeamInvitationAcceptedForHostEvent;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamInvitationRepository;

@UseCase
public class AcceptInvitationUseCase {

    private final TeamInvitationRepository teamInvitationRepository;
    private final OutboxEventService outboxEventService;

    public AcceptInvitationUseCase(TeamInvitationRepository teamInvitationRepository, OutboxEventService outboxEventService) {
        this.teamInvitationRepository = teamInvitationRepository;
        this.outboxEventService = outboxEventService;
    }

    public void acceptInvitation(String id) {
        final TeamInvitation teamInvitation = teamInvitationRepository.findById(id)
                .orElseThrow(() -> new TeamException("Invitation not found", null, Errors.TEAM_INVITATION_NOTFOUND_OR_EXPIRED));

        if(teamInvitation.isExpired()) {
            teamInvitation.markAsExpired();
            teamInvitationRepository.save(teamInvitation);
            throw new TeamException("Invalid status", null, Errors.TEAM_INVITATION_INVALID_STATUS);
        }
        teamInvitationRepository.save(teamInvitation.accept());

        outboxEventService.storeOutboxEvent(TeamInvitationAcceptedForHostEvent.of(teamInvitation));
        outboxEventService.storeOutboxEvent(TeamInvitationAcceptedByGuestEvent.of(teamInvitation));
    }
}
