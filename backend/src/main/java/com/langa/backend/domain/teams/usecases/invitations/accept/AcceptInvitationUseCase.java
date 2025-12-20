package com.langa.backend.domain.teams.usecases.invitations.accept;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamRepository;

@UseCase
public class AcceptInvitationUseCase implements IAcceptInvitationInvitationUseCase, CommandHandler<AcceptInvitationCommand, TeamInvitation> {

    private final TeamRepository teamRepository;
    private final OutboxEventService outboxEventService;

    public AcceptInvitationUseCase(TeamRepository teamRepository, OutboxEventService outboxEventService) {
        this.teamRepository = teamRepository;
        this.outboxEventService = outboxEventService;
    }

    @Override
    public TeamInvitation execute(AcceptInvitationCommand command) {
        final Team team = teamRepository.findById(command.teamId())
                 .orElseThrow(() -> new TeamException("Team not found", null, Errors.TEAM_NOT_FOUND));
        final TeamInvitation teamInvitation = team.getInvitation(command.invitationToken());

        if (teamInvitation == null) {
            throw new TeamException("Invitation not found", null, Errors.TEAM_INVITATION_NOTFOUND_OR_EXPIRED);
        }

        if (!teamInvitation.canAccept(command.guest())) {
            throw new TeamException("Invalid invitation", null, Errors.ACCESS_DENIED);
        }

        if (team.acceptInvitation(teamInvitation)) {
            teamRepository.save(team);
            handleDomainEvents(team);
            return team.getInvitation(command.invitationToken());
        } else {
            teamRepository.save(team);
            throw new TeamException("Invalid invitation", null, Errors.TEAM_INVITATION_NOTFOUND_OR_EXPIRED);
        }
    }

    private void handleDomainEvents(Team team) {
        team.getEvents().forEach(outboxEventService::storeOutboxEvent);
        team.clearEvents();
    }

    @Override
    public TeamInvitation handle(AcceptInvitationCommand command) {
        return execute(command);
    }
}
