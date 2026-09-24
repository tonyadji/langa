package com.langa.backend.infra.rest.teams;

import com.langa.backend.common.commands.CommandBusDispatcher;
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
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import com.langa.backend.domain.teams.usecases.invitations.accept.AcceptInvitationCommand;

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
                new User("guest@example.com", "", java.util.List.of()), "team-1", "token-1");

        assertEquals(InvitationStatus.ACCEPTED, response.getBody().status());
        ArgumentCaptor<AcceptInvitationCommand> command = ArgumentCaptor.forClass(AcceptInvitationCommand.class);
        org.mockito.Mockito.verify(commandBusDispatcher).dispatch(command.capture());
        assertEquals("guest@example.com", command.getValue().guest(), "the guest is the signed-in user");
    }
}
