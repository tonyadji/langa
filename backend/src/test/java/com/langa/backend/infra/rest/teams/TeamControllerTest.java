package com.langa.backend.infra.rest.teams;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.usecases.fetch.IFetchTeamsUseCase;
import com.langa.backend.infra.rest.teams.dto.CreateTeamRequestDto;
import com.langa.backend.infra.rest.teams.dto.TeamResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamControllerTest {

    @Mock
    private CommandBusDispatcher commandBusDispatcher;
    @Mock
    private IFetchTeamsUseCase fetchTeamUseCase;

    private final UserDetails userDetails = new User("owner@example.com", "pw", List.of());

    private TeamController controller() {
        return new TeamController(commandBusDispatcher, fetchTeamUseCase);
    }

    @Test
    void createTeam_shouldReturnCreatedWithDto() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        when(commandBusDispatcher.dispatch(any())).thenReturn(team);

        ResponseEntity<TeamResponseDto> response = controller().createTeam(userDetails, new CreateTeamRequestDto("Dev Team"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Dev Team", response.getBody().name());
    }

    @Test
    void queryTeams_shouldReturnList() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        when(fetchTeamUseCase.queryTeams("owner@example.com")).thenReturn(List.of(team));

        ResponseEntity<List<TeamResponseDto>> response = controller().queryTeams(userDetails);

        assertEquals(1, response.getBody().size());
    }

    @Test
    void queryTeam_shouldReturnSingleTeam() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        when(fetchTeamUseCase.query(any())).thenReturn(team);

        ResponseEntity<TeamResponseDto> response = controller().queryTeam(userDetails, team.getId());

        assertEquals("Dev Team", response.getBody().name());
    }
}
