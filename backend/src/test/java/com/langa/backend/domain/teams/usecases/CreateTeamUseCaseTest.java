package com.langa.backend.domain.teams.usecases;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamMemberRepository;
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
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private CreateTeamUseCase createTeamUseCase;

    @Test
    void shouldCreateTeamSuccessfully() {
        String name = "Team A";
        String ownerEmail = "owner@example.com";
        Team team = Team.createNew(name, ownerEmail, LocalDateTime.now());
        when(teamRepository.findByOwnerAndName(ownerEmail, name)).thenReturn(Optional.empty());
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        Team result = createTeamUseCase.createTeam(name, ownerEmail);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(ownerEmail, result.getCreatedBy());
        verify(teamRepository, times(1)).findByOwnerAndName(ownerEmail, name);
        verify(teamRepository, times(1)).save(any(Team.class));
        verify(teamMemberRepository, times(1)).saveAll(result.getMembers());
    }

    @Test
    void shouldThrowExceptionWhenTeamNameAlreadyExists() {
        String name = "Team A";
        String ownerEmail = "owner@example.com";
        Team existingTeam = Team.createNew(name, ownerEmail, LocalDateTime.now());
        when(teamRepository.findByOwnerAndName(ownerEmail, name)).thenReturn(Optional.of(existingTeam));

        TeamException exception = assertThrows(TeamException.class, () -> createTeamUseCase.createTeam(name, ownerEmail));

        assertEquals("Team name already exists", exception.getMessage());
        assertEquals(Errors.TEAM_NAME_ALREADY_EXISTS, exception.getError());
        verify(teamRepository, times(1)).findByOwnerAndName(ownerEmail, name);
        verify(teamRepository, never()).save(any(Team.class));
        verify(teamMemberRepository, never()).saveAll(anyList());
    }
}