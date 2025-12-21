package com.langa.backend.infra.rest.teams;


import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.domain.teams.usecases.invitations.accept.AcceptInvitationCommand;
import com.langa.backend.domain.teams.valueobjects.TeamInvitation;
import com.langa.backend.infra.rest.teams.dto.AcceptInvitationRequest;
import com.langa.backend.infra.rest.teams.dto.GetInvitationResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/team-invitations")
@CrossOrigin(origins = "*")
public class AcceptInvitationController {

    private final CommandBusDispatcher commandBusDispatcher;

    public AcceptInvitationController(CommandBusDispatcher commandBusDispatcher) {
        this.commandBusDispatcher = commandBusDispatcher;
    }

    @PostMapping("{teamId}/accept")
    public ResponseEntity<GetInvitationResponseDto> acceptInvitation(@PathVariable String teamId,
                                                                     @RequestParam String invitationToken,
                                                                     @RequestBody AcceptInvitationRequest request) {
        final TeamInvitation invitation = commandBusDispatcher.dispatch(new AcceptInvitationCommand(teamId, invitationToken, request.guest()));
        return ResponseEntity.ok(GetInvitationResponseDto.of(invitation));
    }
}
