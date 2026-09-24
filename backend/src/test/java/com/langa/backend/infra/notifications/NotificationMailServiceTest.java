package com.langa.backend.infra.notifications;

import com.langa.backend.infra.notifications.mail.EmailNotification;
import com.langa.backend.infra.notifications.mail.services.MailSendService;
import com.langa.backend.infra.notifications.model.Notification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NotificationMailServiceTest {

    @Mock
    private MailSendService mailSendService;

    @Test
    void send_shouldPingFirstRecipient_forEmailNotification() {
        NotificationMailService service = new NotificationMailService(mailSendService);
        EmailNotification notification = new EmailNotification()
                .setRecipients(List.of("first@example.com", "second@example.com"))
                .setSubject("Subject")
                .setBody("Body");

        service.send(notification);

        verify(mailSendService).ping("first@example.com", "Subject", "Body");
    }

    @Test
    void send_shouldDoNothing_forNonEmailNotification() {
        NotificationMailService service = new NotificationMailService(mailSendService);
        Notification notification = () -> com.langa.backend.infra.notifications.model.NotificationType.EMAIL;

        service.send(notification);

        verifyNoInteractions(mailSendService);
    }
}
