package com.langa.backend.domain.teams.usecases.invitations.fetch;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamRepository;

import java.util.Objects;

@UseCase
public class GetInvitationUseCase {

    private final TeamRepository teamRepository;
    private final OutboxEventService outboxEventService;

    public GetInvitationUseCase(TeamRepository teamRepository, OutboxEventService outboxEventService) {
        this.teamRepository = teamRepository;
        this.outboxEventService = outboxEventService;
    }

    public TeamInvitation query(GetInvitationQuery query) {
        final Team team = teamRepository.findById(query.teamId())
                .orElseThrow(() -> new TeamException("Team not found with id " + query.teamId(), null, Errors.TEAM_NOT_FOUND));

        final TeamInvitation teamInvitation = team.getInvitations().stream()
                .filter(invitation -> Objects.equals(invitation.getIdentity().invitationToken(), query.invitationToken()))
                .findFirst().orElse(null);

        if (teamInvitation == null) {
            throw new TeamException("Invitation not found", null, Errors.TEAM_INVITATION_NOTFOUND_OR_EXPIRED);
        }

        if (teamInvitation.isExpired()) {
            team.flagInvitationExpired(teamInvitation);
            teamRepository.save(team);
            handleDomainEvents(team);
            throw new TeamException("Invitation not found", null, Errors.TEAM_INVITATION_NOTFOUND_OR_EXPIRED);
        }

        return teamInvitation;
    }

    private void handleDomainEvents(Team team) {
        team.getEvents().forEach(outboxEventService::storeOutboxEvent);
        team.clearEvents();
    }
}
