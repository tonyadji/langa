package com.langa.backend.infra.rest.teams;


import com.langa.backend.domain.teams.usecases.invitations.fetch.GetInvitationQuery;
import com.langa.backend.domain.teams.usecases.invitations.fetch.GetInvitationUseCase;
import com.langa.backend.domain.teams.usecases.invitations.fetch.GetPublicInvitationQuery;
import com.langa.backend.infra.rest.teams.dto.GetInvitationResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/team-invitations")
@CrossOrigin(origins = "*")
public class GetTeamInvitationController {

    private final GetInvitationUseCase getInvitationUseCase;

    public GetTeamInvitationController(GetInvitationUseCase getInvitationUseCase) {
        this.getInvitationUseCase = getInvitationUseCase;
    }

    @GetMapping("{teamId}")
    public ResponseEntity<GetInvitationResponseDto> getInvitation(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String teamId, @RequestParam String invitationToken) {
        return ResponseEntity.ok(GetInvitationResponseDto.of(
                getInvitationUseCase.query(new GetInvitationQuery(teamId, invitationToken, userDetails.getUsername()))));
    }

    @GetMapping("{teamId}/public")
    public ResponseEntity<GetInvitationResponseDto> getInvitation(@PathVariable String teamId, @RequestParam String invitationToken) {
        return ResponseEntity.ok(GetInvitationResponseDto.of(
                getInvitationUseCase.query(new GetPublicInvitationQuery(teamId, invitationToken))));
    }
}
