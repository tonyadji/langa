package com.langa.backend.infra.rest.teams;

import com.langa.backend.domain.teams.usecases.invitations.fetch.GetInvitationQuery;
import com.langa.backend.domain.teams.usecases.invitations.fetch.GetInvitationUseCase;
import com.langa.backend.domain.teams.usecases.invitations.fetch.GetPublicInvitationQuery;
import com.langa.backend.domain.teams.valueobjects.InvitationStatus;
import com.langa.backend.domain.teams.valueobjects.TeamInvitation;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationIdentity;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationPeriod;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationStakeHolders;
import com.langa.backend.infra.rest.teams.dto.GetInvitationResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTeamInvitationControllerTest {

    @Mock
    private GetInvitationUseCase getInvitationUseCase;

    private TeamInvitation invitation() {
        return TeamInvitation.populate(
                new TeamInvitationIdentity("team-1", "token-1"),
                new TeamInvitationStakeHolders("team-key", "host@example.com", "guest@example.com"),
                new TeamInvitationPeriod(LocalDateTime.now(), LocalDateTime.now().plusDays(1)),
                null, InvitationStatus.SENT);
    }

    @Test
    void getInvitation_authenticated_shouldReturnInvitation() {
        GetTeamInvitationController controller = new GetTeamInvitationController(getInvitationUseCase);
        UserDetails userDetails = new User("guest@example.com", "pw", List.of());
        when(getInvitationUseCase.query(any(GetInvitationQuery.class))).thenReturn(invitation());

        ResponseEntity<GetInvitationResponseDto> response = controller.getInvitation(userDetails, "team-1", "token-1");

        assertEquals("guest@example.com", response.getBody().guest());
    }

    @Test
    void getInvitation_public_shouldReturnInvitation() {
        GetTeamInvitationController controller = new GetTeamInvitationController(getInvitationUseCase);
        when(getInvitationUseCase.query(any(GetPublicInvitationQuery.class))).thenReturn(invitation());

        ResponseEntity<GetInvitationResponseDto> response = controller.getInvitation("team-1", "token-1");

        assertEquals("guest@example.com", response.getBody().guest());
    }
}
