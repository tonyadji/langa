package com.langa.backend.infra.rest.teams;


import com.langa.backend.domain.teams.usecases.AcceptInvitationUseCase;
import com.langa.backend.domain.teams.usecases.GetInvitationUseCase;
import com.langa.backend.infra.rest.teams.dto.AcceptInvitationRequest;
import com.langa.backend.infra.rest.teams.dto.GetInvitationResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/team-invitations")
@CrossOrigin(origins = "*")
public class TeamInvitationController {

    private final GetInvitationUseCase getInvitationUseCase;
    private final AcceptInvitationUseCase acceptInvitationUseCase;

    public TeamInvitationController(GetInvitationUseCase getInvitationUseCase, AcceptInvitationUseCase acceptInvitationUseCase) {
        this.getInvitationUseCase = getInvitationUseCase;
        this.acceptInvitationUseCase = acceptInvitationUseCase;
    }

    @GetMapping
    public ResponseEntity<GetInvitationResponseDto> getInvitation(@RequestParam String invitationToken) {
        return ResponseEntity.ok(GetInvitationResponseDto.of(
                getInvitationUseCase.getInvitation(invitationToken)));
    }

    @PostMapping("/accept")
    public ResponseEntity<String> getInvitation(@Valid @RequestBody AcceptInvitationRequest acceptInvitationRequest) {
        acceptInvitationUseCase.acceptInvitation(acceptInvitationRequest.invitationId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Invitation accepted");
    }
}
