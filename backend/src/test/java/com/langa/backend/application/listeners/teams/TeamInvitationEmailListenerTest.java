package com.langa.backend.application.listeners.teams;

import com.langa.backend.domain.teams.events.TeamInvitationEmailEvent;
import com.langa.backend.infra.notifications.NotificationService;
import com.langa.backend.infra.notifications.builders.MailNotificationBuilder;
import com.langa.backend.infra.notifications.model.Notification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamInvitationEmailListenerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private MailNotificationBuilder mailNotificationBuilder;

    @Test
    void handleTeamInvitationEmailEvent_shouldSendNotification() {
        TeamInvitationEmailListener listener = new TeamInvitationEmailListener(notificationService, mailNotificationBuilder);
        TeamInvitationEmailEvent event = new TeamInvitationEmailEvent(
                "team-1", "guest@example.com", "host@example.com", "Dev Team", "team-key", "token-1", LocalDateTime.now());
        Notification notification = mock(Notification.class);
        when(mailNotificationBuilder.build(event)).thenReturn(notification);

        listener.handleTeamInvitationEmailEvent(event);

        verify(notificationService).send(notification);
    }

    @Test
    void handleTeamInvitationEmailEvent_shouldSwallowException() {
        TeamInvitationEmailListener listener = new TeamInvitationEmailListener(notificationService, mailNotificationBuilder);
        TeamInvitationEmailEvent event = new TeamInvitationEmailEvent(
                "team-1", "guest@example.com", "host@example.com", "Dev Team", "team-key", "token-1", LocalDateTime.now());
        when(mailNotificationBuilder.build(event)).thenThrow(new RuntimeException("boom"));

        listener.handleTeamInvitationEmailEvent(event);

        verifyNoInteractions(notificationService);
    }
}
