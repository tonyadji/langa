package com.langa.backend.infra.rest.teams;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.infra.rest.teams.dto.InviteMemberRequestDto;
import com.langa.backend.infra.rest.teams.dto.TeamResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendInvitationControllerTest {

    @Mock
    private CommandBusDispatcher commandBusDispatcher;

    @Test
    void inviteMember_shouldReturnCreatedWithTeamDto() {
        SendInvitationController controller = new SendInvitationController(commandBusDispatcher);
        UserDetails userDetails = new User("owner@example.com", "pw", List.of());
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        when(commandBusDispatcher.dispatch(any())).thenReturn(team);

        ResponseEntity<TeamResponseDto> response = controller.inviteMember(
                userDetails, new InviteMemberRequestDto("guest@example.com", team.getId()));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Dev Team", response.getBody().name());
    }
}
