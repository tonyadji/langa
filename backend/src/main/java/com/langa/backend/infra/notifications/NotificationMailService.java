package com.langa.backend.infra.notifications;

import com.langa.backend.infra.notifications.mail.services.MailSendService;
import com.langa.backend.infra.notifications.model.Notification;
import com.langa.backend.infra.notifications.mail.EmailNotification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMailService implements NotificationService {

    private final MailSendService mailSendService;

    public NotificationMailService(MailSendService mailSendService) {
        this.mailSendService = mailSendService;
    }

    @Override
    public void send(Notification notification) {
        if (notification instanceof EmailNotification emailNotification) {
            mailSendService.ping(
                    emailNotification.getRecipients().getFirst(),
                    emailNotification.getSubject(),
                    emailNotification.getBody()
            );
        }
    }
}
