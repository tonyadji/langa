package com.langa.backend.domain.teams.usecases.invitations.send;

import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendInvitationUseCaseTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private SendInvitationUseCase useCase;

    @Test
    void execute_shouldSendInvitation_whenHostIsOwner() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        Team result = useCase.execute(new SendInvitationCommand("guest@example.com", team.getId(), "owner@example.com"));

        assertEquals(1, result.getInvitations().size());
    }

    @Test
    void execute_shouldThrow_whenTeamNotFound() {
        when(teamRepository.findById("team-1")).thenReturn(Optional.empty());

        TeamException ex = assertThrows(TeamException.class,
                () -> useCase.execute(new SendInvitationCommand("guest@example.com", "team-1", "owner@example.com")));

        assertEquals(Errors.TEAM_NOT_FOUND, ex.getError());
    }

    @Test
    void execute_shouldThrow_whenHostIsNotOwner() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        assertThrows(TeamException.class,
                () -> useCase.execute(new SendInvitationCommand("guest@example.com", team.getId(), "intruder@example.com")));
    }

    @Test
    void handle_shouldDelegateToExecute() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        Team result = useCase.handle(new SendInvitationCommand("guest@example.com", team.getId(), "owner@example.com"));

        assertEquals(1, result.getInvitations().size());
    }
}
