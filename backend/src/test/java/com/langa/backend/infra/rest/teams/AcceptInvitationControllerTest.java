package com.langa.backend.infra.rest.teams;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.domain.teams.valueobjects.InvitationStatus;
import com.langa.backend.domain.teams.valueobjects.TeamInvitation;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationIdentity;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationPeriod;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationStakeHolders;
import com.langa.backend.infra.rest.teams.dto.AcceptInvitationRequest;
import com.langa.backend.infra.rest.teams.dto.GetInvitationResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcceptInvitationControllerTest {

    @Mock
    private CommandBusDispatcher commandBusDispatcher;

    @Test
    void acceptInvitation_shouldReturnAcceptedInvitation() {
        AcceptInvitationController controller = new AcceptInvitationController(commandBusDispatcher);
        TeamInvitation invitation = TeamInvitation.populate(
                new TeamInvitationIdentity("team-1", "token-1"),
                new TeamInvitationStakeHolders("team-key", "host@example.com", "guest@example.com"),
                new TeamInvitationPeriod(LocalDateTime.now(), LocalDateTime.now().plusDays(1)),
                LocalDateTime.now(), InvitationStatus.ACCEPTED);
        when(commandBusDispatcher.dispatch(any())).thenReturn(invitation);

        ResponseEntity<GetInvitationResponseDto> response = controller.acceptInvitation(
                "team-1", "token-1", new AcceptInvitationRequest("guest@example.com", "token-1"));

        assertEquals(InvitationStatus.ACCEPTED, response.getBody().status());
    }
}
