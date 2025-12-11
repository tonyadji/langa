package com.langa.backend.infra.rest.teams;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.infra.rest.teams.dto.InviteMemberRequestDto;
import com.langa.backend.infra.rest.teams.dto.TeamResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams/invite")
@CrossOrigin(origins = "*")
public class SendInvitationController {

    private final CommandBusDispatcher commandBusDispatcher;

    public SendInvitationController(CommandBusDispatcher commandBusDispatcher) {
        this.commandBusDispatcher = commandBusDispatcher;
    }

    @PostMapping
    public ResponseEntity<TeamResponseDto> inviteMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InviteMemberRequestDto inviteMemberRequestDto) {
        final Team team = commandBusDispatcher.dispatch(inviteMemberRequestDto.toCommand(userDetails.getUsername()));
        return ResponseEntity.status(HttpStatus.CREATED).body(TeamResponseDto.of(team));
    }
}
