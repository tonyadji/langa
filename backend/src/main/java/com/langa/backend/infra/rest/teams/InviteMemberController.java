package com.langa.backend.infra.rest.teams;

import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.domain.teams.usecases.SendInvitationUseCase;
import com.langa.backend.infra.rest.teams.dto.InviteMemberRequestDto;
import com.langa.backend.infra.rest.teams.dto.InviteMemberResponseDto;
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

    private final SendInvitationUseCase sendInvitationUseCase;

    public InviteMemberController(SendInvitationUseCase sendInvitationUseCase) {
        this.sendInvitationUseCase = sendInvitationUseCase;
    }

    @PostMapping
    public ResponseEntity<InviteMemberResponseDto> inviteMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InviteMemberRequestDto inviteMemberRequestDto) {

        final TeamInvitation invitation = sendInvitationUseCase.invite(inviteMemberRequestDto.toTeamInvitation(userDetails.getUsername()));

        return ResponseEntity.status(HttpStatus.CREATED).body(InviteMemberResponseDto.of(invitation));
    }
}
