package com.langa.backend.domain.teams.usecases.fetch;

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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTeamsUseCaseTest {

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private GetTeamsUseCase useCase;

    @Test
    void query_shouldReturnTeam_whenOwnerAndMember() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        Team result = useCase.query(new GetTeamQuery(team.getId(), "owner@example.com"));

        assertEquals(team, result);
    }

    @Test
    void query_shouldThrow_whenTeamNotFound() {
        when(teamRepository.findById("team-1")).thenReturn(Optional.empty());

        TeamException ex = assertThrows(TeamException.class,
                () -> useCase.query(new GetTeamQuery("team-1", "owner@example.com")));

        assertEquals(Errors.TEAM_NOT_FOUND, ex.getError());
    }

    @Test
    void query_shouldThrow_whenCallerIsNotOwner() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        assertThrows(TeamException.class,
                () -> useCase.query(new GetTeamQuery(team.getId(), "intruder@example.com")));
    }

    @Test
    void queryTeams_shouldReturnTeamsForOwner() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        when(teamRepository.findByOwnerOrTeamMember("owner@example.com")).thenReturn(List.of(team));

        List<Team> result = useCase.queryTeams("owner@example.com");

        assertEquals(1, result.size());
    }
}
