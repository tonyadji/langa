package com.langa.backend.domain.teams.usecases.invitations.send;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamRepository;

@UseCase
public class SendInvitationUseCase implements CommandHandler<SendInvitationCommand, Team>, ISendInvitationUseCase {

    private final TeamRepository teamRepository;
    private final OutboxEventService outboxEventService;

    public SendInvitationUseCase(TeamRepository teamRepository, OutboxEventService outboxEventService) {
        this.teamRepository = teamRepository;
        this.outboxEventService = outboxEventService;
    }


    @Override
    public Team handle(SendInvitationCommand command) {
        return execute(command);
    }

    @Override
    public Team execute(SendInvitationCommand command) {

        final Team team = teamRepository.findById(command.team())
                .orElseThrow(() -> new TeamException("Team not found with id "+ command.team(), null, Errors.TEAM_NOT_FOUND));

        team.checkOwnership(command.host());

        team.invite(command.guest());

        teamRepository.save(team);
        handleDomainEvents(team);
        return team;
    }

    private void handleDomainEvents(Team team) {
        team.getEvents().forEach(outboxEventService::storeOutboxEvent);
        team.clearEvents();
    }
}
