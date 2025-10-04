package com.langa.backend.infra.notifications;

import com.langa.backend.infra.notifications.mail.services.MailSendService;
import com.langa.backend.infra.notifications.model.DomainNotification;
import com.langa.backend.infra.notifications.model.EmailNotification;
import org.springframework.stereotype.Component;

@Component
public class MailServiceImpl implements NotificationService {

    private final MailSendService mailSendService;

    public MailServiceImpl(MailSendService mailSendService) {
        this.mailSendService = mailSendService;
    }

    @Override
    public void send(DomainNotification notification) {
        if (notification instanceof EmailNotification emailNotification) {
            mailSendService.ping(
                    emailNotification.getRecipients().get(0),
                    emailNotification.getSubject(),
                    emailNotification.getBody()
            );
        }
    }
}
