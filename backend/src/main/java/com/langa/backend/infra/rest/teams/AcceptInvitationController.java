package com.langa.backend.infra.rest.teams;


import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.domain.teams.usecases.invitations.accept.AcceptInvitationCommand;
import com.langa.backend.domain.teams.valueobjects.TeamInvitation;
import com.langa.backend.infra.rest.teams.dto.GetInvitationResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/team-invitations")
public class AcceptInvitationController {

    public static final String ACCEPT_PATH = "/api/team-invitations/*/accept";

    private final CommandBusDispatcher commandBusDispatcher;

    public AcceptInvitationController(CommandBusDispatcher commandBusDispatcher) {
        this.commandBusDispatcher = commandBusDispatcher;
    }

    /**
     * Accepts an invitation on behalf of the signed-in user: the guest is the authenticated user,
     * never a value sent by the client (a request body, if any, is ignored).
     */
    @PostMapping("{teamId}/accept")
    public ResponseEntity<GetInvitationResponseDto> acceptInvitation(@AuthenticationPrincipal UserDetails userDetails,
                                                                     @PathVariable String teamId,
                                                                     @RequestParam String invitationToken) {
        final TeamInvitation invitation = commandBusDispatcher.dispatch(
                new AcceptInvitationCommand(teamId, invitationToken, userDetails.getUsername()));
        return ResponseEntity.ok(GetInvitationResponseDto.of(invitation));
    }
}
