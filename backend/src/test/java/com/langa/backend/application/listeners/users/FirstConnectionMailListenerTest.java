package com.langa.backend.application.listeners.users;

import com.langa.backend.domain.users.events.FirstConnectionMailEvent;
import com.langa.backend.infra.notifications.NotificationService;
import com.langa.backend.infra.notifications.builders.MailNotificationBuilder;
import com.langa.backend.infra.notifications.model.Notification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirstConnectionMailListenerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private MailNotificationBuilder mailNotificationBuilder;

    @Test
    void handleTeamInvitationEmailEvent_shouldSendNotification() {
        FirstConnectionMailListener listener = new FirstConnectionMailListener(notificationService, mailNotificationBuilder);
        FirstConnectionMailEvent event = new FirstConnectionMailEvent("acc-1", "user@example.com");
        Notification notification = mock(Notification.class);
        when(mailNotificationBuilder.build(event)).thenReturn(notification);

        listener.handleTeamInvitationEmailEvent(event);

        verify(notificationService).send(notification);
    }

    @Test
    void handleTeamInvitationEmailEvent_shouldSwallowException() {
        FirstConnectionMailListener listener = new FirstConnectionMailListener(notificationService, mailNotificationBuilder);
        FirstConnectionMailEvent event = new FirstConnectionMailEvent("acc-1", "user@example.com");
        when(mailNotificationBuilder.build(event)).thenThrow(new RuntimeException("boom"));

        listener.handleTeamInvitationEmailEvent(event);

        verifyNoInteractions(notificationService);
    }
}
