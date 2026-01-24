package com.langa.backend.infra.notifications.builders;

import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.infra.notifications.builders.templates.EmailTemplate;
import com.langa.backend.infra.notifications.exceptions.NotificationException;
import com.langa.backend.infra.notifications.model.Notification;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MailNotificationBuilder extends AbstractNotificationBuilder {

    private final List<EmailTemplate> templates;

    public MailNotificationBuilder(List<EmailTemplate> templates) {
        super();
        this.templates = templates;
    }

    public Notification build(DomainEvent event) {
        final EmailTemplate emailTemplate = templates.stream()
                .filter(template -> template.couldProcess(event))
                .findFirst()
                .orElseThrow(() -> new NotificationException("Unable to find a template to process this event", null, Errors.INTERNAL_SERVER_ERROR));

        emailTemplate.processEvent(event);

        return NotificationBuilder.getBuilder()
                .withSubject(emailTemplate.getSubject())
                .withRecipients(emailTemplate.getRecipients())
                .withBody(emailTemplate.getMessage())
                .buildEmailNotification();
    }
}
