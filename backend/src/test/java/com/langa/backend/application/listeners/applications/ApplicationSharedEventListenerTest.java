package com.langa.backend.application.listeners.applications;

import com.langa.backend.domain.applications.events.ApplicationSharedEvent;
import com.langa.backend.infra.notifications.NotificationService;
import com.langa.backend.infra.notifications.builders.MailNotificationBuilder;
import com.langa.backend.infra.notifications.model.Notification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationSharedEventListenerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private MailNotificationBuilder mailNotificationBuilder;

    @Test
    void handleTeamInvitationEmailEvent_shouldSendNotification() {
        ApplicationSharedEventListener listener = new ApplicationSharedEventListener(notificationService, mailNotificationBuilder);
        ApplicationSharedEvent event = new ApplicationSharedEvent("app-1", "owner@example.com", "guest@example.com", "My App");
        Notification notification = mock(Notification.class);
        when(mailNotificationBuilder.build(event)).thenReturn(notification);

        listener.handleTeamInvitationEmailEvent(event);

        verify(notificationService).send(notification);
    }

    @Test
    void handleTeamInvitationEmailEvent_shouldSwallowException() {
        ApplicationSharedEventListener listener = new ApplicationSharedEventListener(notificationService, mailNotificationBuilder);
        ApplicationSharedEvent event = new ApplicationSharedEvent("app-1", "owner@example.com", "guest@example.com", "My App");
        when(mailNotificationBuilder.build(event)).thenThrow(new RuntimeException("boom"));

        listener.handleTeamInvitationEmailEvent(event);

        verifyNoInteractions(notificationService);
    }
}
