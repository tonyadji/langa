package com.langa.backend.application.listeners.teams;

import com.langa.backend.domain.users.valueobjects.ExternalIdentity;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.events.InvitationAcceptedMailEvent;
import com.langa.backend.domain.teams.events.TeamInvitationAcceptedByGuestEvent;
import com.langa.backend.domain.teams.services.TeamMemberShipService;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.events.FirstConnectionMailEvent;
import com.langa.backend.domain.users.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamInvitationAcceptedByGuestListenerTest {

    @Mock
    private UserService userService;
    @Mock
    private TeamMemberShipService teamMemberShipService;
    @Mock
    private OutboxEventService outboxEventService;

    @Test
    void handleTeamInvitationAcceptedByGuestEvent_shouldAddMemberAndSendFirstConnectionMail_whenGuestNeverSignedIn() {
        TeamInvitationAcceptedByGuestListener listener =
                new TeamInvitationAcceptedByGuestListener(userService, teamMemberShipService, outboxEventService);
        TeamInvitationAcceptedByGuestEvent event = new TeamInvitationAcceptedByGuestEvent(
                "team-1", "guest@example.com", "host@example.com", "team-key", "token-1", LocalDateTime.now());
        User user = User.createInvited("guest@example.com");
        Team team = Team.createNew("Dev Team", "host@example.com", LocalDateTime.now());

        when(userService.findOrCreateUserByEmail("guest@example.com")).thenReturn(user);
        when(teamMemberShipService.addMemberToTeam("team-key", "guest@example.com")).thenReturn(team);

        listener.handleTeamInvitationAcceptedByGuestEvent(event);

        verify(outboxEventService).storeOutboxEvent(any(InvitationAcceptedMailEvent.class));
        verify(outboxEventService).storeOutboxEvent(any(FirstConnectionMailEvent.class));
    }

    @Test
    void handleTeamInvitationAcceptedByGuestEvent_shouldNotSendFirstConnectionMail_whenGuestAlreadySignedIn() {
        TeamInvitationAcceptedByGuestListener listener =
                new TeamInvitationAcceptedByGuestListener(userService, teamMemberShipService, outboxEventService);
        TeamInvitationAcceptedByGuestEvent event = new TeamInvitationAcceptedByGuestEvent(
                "team-1", "guest@example.com", "host@example.com", "team-key", "token-1", LocalDateTime.now());
        User user = User.createFromExternalIdentity(new ExternalIdentity("entra", "oid-1", "guest@example.com"));
        Team team = Team.createNew("Dev Team", "host@example.com", LocalDateTime.now());

        when(userService.findOrCreateUserByEmail("guest@example.com")).thenReturn(user);
        when(teamMemberShipService.addMemberToTeam("team-key", "guest@example.com")).thenReturn(team);

        listener.handleTeamInvitationAcceptedByGuestEvent(event);

        verify(outboxEventService).storeOutboxEvent(any(InvitationAcceptedMailEvent.class));
        verify(outboxEventService, never()).storeOutboxEvent(any(FirstConnectionMailEvent.class));
    }

    @Test
    void handleTeamInvitationAcceptedByGuestEvent_shouldSwallowException() {
        TeamInvitationAcceptedByGuestListener listener =
                new TeamInvitationAcceptedByGuestListener(userService, teamMemberShipService, outboxEventService);
        TeamInvitationAcceptedByGuestEvent event = new TeamInvitationAcceptedByGuestEvent(
                "team-1", "guest@example.com", "host@example.com", "team-key", "token-1", LocalDateTime.now());
        when(userService.findOrCreateUserByEmail("guest@example.com")).thenThrow(new RuntimeException("boom"));

        listener.handleTeamInvitationAcceptedByGuestEvent(event);

        verifyNoInteractions(outboxEventService);
    }
}
