package com.langa.backend.infra.rest.teams;

import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.usecases.CreateTeamUseCase;
import com.langa.backend.infra.rest.teams.dto.CreateTeamRequestDto;
import com.langa.backend.infra.rest.teams.dto.CreateTeamResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {

    private final CreateTeamUseCase createTeamUseCase;

    public TeamController(CreateTeamUseCase createTeamUseCase) {
        this.createTeamUseCase = createTeamUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateTeamResponseDto> createTeam(@AuthenticationPrincipal UserDetails userDetails,
                                                            @RequestBody @Valid CreateTeamRequestDto createTeamRequestDto) {
        final Team team = createTeamUseCase.createTeam(createTeamRequestDto.name(), userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(CreateTeamResponseDto.of(team));
    }
}
