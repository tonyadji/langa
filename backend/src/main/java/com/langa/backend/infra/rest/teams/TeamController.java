package com.langa.backend.infra.rest.teams;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.usecases.fetch.GetTeamQuery;
import com.langa.backend.domain.teams.usecases.fetch.IFetchTeamsUseCase;
import com.langa.backend.infra.rest.teams.dto.CreateTeamRequestDto;
import com.langa.backend.infra.rest.teams.dto.TeamResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {

    private final CommandBusDispatcher commandBusDispatcher;
    private final IFetchTeamsUseCase fetchTeamUse;

    public TeamController(CommandBusDispatcher commandBusDispatcher, IFetchTeamsUseCase fetchTeamUse) {
        this.commandBusDispatcher = commandBusDispatcher;
        this.fetchTeamUse = fetchTeamUse;
    }

    @PostMapping
    public ResponseEntity<TeamResponseDto> createTeam(@AuthenticationPrincipal UserDetails userDetails,
                                                      @RequestBody @Valid CreateTeamRequestDto createTeamRequestDto) {
        final Team team = commandBusDispatcher.dispatch(createTeamRequestDto.toCommand(userDetails.getUsername()));
        return ResponseEntity.status(HttpStatus.CREATED).body(TeamResponseDto.of(team));
    }

    @GetMapping
    public ResponseEntity<List<TeamResponseDto>> createTeam(@AuthenticationPrincipal UserDetails userDetails) {
        final List<Team> teams = fetchTeamUse.queryTeams(userDetails.getUsername());
        return ResponseEntity.ok(TeamResponseDto.of(teams));
    }

    @GetMapping("{teamId}")
    public ResponseEntity<TeamResponseDto> createTeam(@AuthenticationPrincipal UserDetails userDetails,
                                                      @PathVariable String teamId) {
        final Team team = fetchTeamUse.query(new GetTeamQuery(teamId, userDetails.getUsername()));
        return ResponseEntity.ok(TeamResponseDto.of(team));
    }

}
