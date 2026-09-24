package com.langa.backend.domain.teams.usecases.create;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTeamUseCaseTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private CreateTeamUseCase useCase;

    @Test
    void execute_shouldCreateTeam_whenNameNotUsed() {
        when(teamRepository.findByOwnerAndName("owner@example.com", "Dev Team")).thenReturn(Optional.empty());
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        Team team = useCase.execute(new CreateTeamCommand("Dev Team", "owner@example.com"));

        assertEquals("Dev Team", team.getName());
        verify(outboxEventService, never()).storeOutboxEvent(any());
    }

    @Test
    void execute_shouldThrow_whenNameAlreadyExists() {
        Team existing = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        when(teamRepository.findByOwnerAndName("owner@example.com", "Dev Team")).thenReturn(Optional.of(existing));

        TeamException ex = assertThrows(TeamException.class,
                () -> useCase.execute(new CreateTeamCommand("Dev Team", "owner@example.com")));

        assertEquals(Errors.TEAM_NAME_ALREADY_EXISTS, ex.getError());
    }

    @Test
    void createTeam_legacyMethod_shouldCreateTeam() {
        when(teamRepository.findByOwnerAndName("owner@example.com", "Dev Team")).thenReturn(Optional.empty());
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        Team team = useCase.createTeam("Dev Team", "owner@example.com");

        assertEquals("Dev Team", team.getName());
    }

    @Test
    void createTeam_legacyMethod_shouldThrow_whenNameAlreadyExists() {
        Team existing = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        when(teamRepository.findByOwnerAndName("owner@example.com", "Dev Team")).thenReturn(Optional.of(existing));

        assertThrows(TeamException.class, () -> useCase.createTeam("Dev Team", "owner@example.com"));
    }

    @Test
    void handle_shouldDelegateToExecute() {
        when(teamRepository.findByOwnerAndName(any(), any())).thenReturn(Optional.empty());
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        Team team = useCase.handle(new CreateTeamCommand("Dev Team", "owner@example.com"));

        assertEquals("Dev Team", team.getName());
    }
}
