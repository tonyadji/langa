package com.langa.backend.domain.teams.usecases.create;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamRepository;

import java.time.LocalDateTime;

@UseCase
public class CreateTeamUseCase implements ICreateTeamUseCase, CommandHandler<CreateTeamCommand, Team> {

    private final TeamRepository teamRepository;
    private final OutboxEventService outboxEventService;

    public CreateTeamUseCase(TeamRepository teamRepository, OutboxEventService outboxEventService) {
        this.teamRepository = teamRepository;
        this.outboxEventService = outboxEventService;
    }

    public Team createTeam(String name, String ownerEmail) {
        if (teamRepository.findByOwnerAndName(ownerEmail, name).isPresent()) {
            throw new TeamException("Team name already exists", null, Errors.TEAM_NAME_ALREADY_EXISTS);
        }
        final Team team = teamRepository.save(Team.createNew(name, ownerEmail, LocalDateTime.now()));
        return team;
    }

    @Override
    public Team handle(CreateTeamCommand command) {
        return execute(command);
    }

    @Override
    public Team execute(CreateTeamCommand command) {
        if (teamRepository.findByOwnerAndName(command.ownerEmail(), command.name()).isPresent()) {
            throw new TeamException("Team name already exists", null, Errors.TEAM_NAME_ALREADY_EXISTS);
        }
        final Team team = teamRepository.save(Team.createNew(command.name(), command.ownerEmail(), LocalDateTime.now()));
        handleDomainEvents(team);
        return team;
    }

    private void handleDomainEvents(Team team) {
        team.getEvents().forEach(outboxEventService::storeOutboxEvent);
        team.clearEvents();
    }
}
