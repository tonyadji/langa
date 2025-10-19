package com.langa.backend.infra.rest.teams;

import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.infra.rest.teams.dto.InviteMemberRequestDto;
import com.langa.backend.infra.rest.teams.dto.InviteMemberResponseDto;
import com.langa.backend.infra.adapters.services.teams.SendInvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams/invite")
@CrossOrigin(origins = "*")
public class InviteMemberController {

    private final SendInvitationService sendInvitationService;

    public InviteMemberController(SendInvitationService sendInvitationService) {
        this.sendInvitationService = sendInvitationService;
    }

    @PostMapping
    public ResponseEntity<InviteMemberResponseDto> inviteMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InviteMemberRequestDto inviteMemberRequestDto) {

        final TeamInvitation invitation = sendInvitationService.invite(inviteMemberRequestDto.toTeamInvitation(userDetails.getUsername()));

        return ResponseEntity.status(HttpStatus.CREATED).body(InviteMemberResponseDto.of(invitation));
    }
}
