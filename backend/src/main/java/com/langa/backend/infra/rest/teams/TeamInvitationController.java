package com.langa.backend.infra.rest.teams;


import com.langa.backend.domain.teams.usecases.GetInvitationUseCase;
import com.langa.backend.infra.rest.teams.dto.GetInvitationResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/team-invitations")
@CrossOrigin(origins = "*")
public class TeamInvitationController {

    private final GetInvitationUseCase getInvitationUseCase;

    public TeamInvitationController(GetInvitationUseCase getInvitationUseCase) {
        this.getInvitationUseCase = getInvitationUseCase;
    }

    @GetMapping
    public ResponseEntity<GetInvitationResponseDto> getInvitation(@RequestParam String invitationToken) {
        return ResponseEntity.ok(GetInvitationResponseDto.of(
                getInvitationUseCase.getInvitation(invitationToken)));
    }

    @PostMapping("/accept")
    public Object getInvitation(@Valid @RequestBody Object acceptInvitationRequest) {
        // call the use case to accept the invitation
        return null;
    }
}
