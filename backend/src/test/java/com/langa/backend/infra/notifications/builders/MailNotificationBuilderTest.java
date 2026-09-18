package com.langa.backend.infra.notifications.builders;

import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.domain.applications.events.ApplicationSharedEvent;
import com.langa.backend.infra.notifications.builders.templates.EmailTemplate;
import com.langa.backend.infra.notifications.exceptions.NotificationException;
import com.langa.backend.infra.notifications.mail.EmailNotification;
import com.langa.backend.infra.notifications.model.Notification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailNotificationBuilderTest {

    @Mock
    private EmailTemplate matchingTemplate;
    @Mock
    private EmailTemplate nonMatchingTemplate;

    @Test
    void build_shouldUseFirstMatchingTemplate() {
        DomainEvent event = new ApplicationSharedEvent("app-1", "owner@example.com", "guest@example.com", "My App");
        when(nonMatchingTemplate.couldProcess(event)).thenReturn(false);
        when(matchingTemplate.couldProcess(event)).thenReturn(true);
        when(matchingTemplate.getSubject()).thenReturn("Subject");
        when(matchingTemplate.getMessage()).thenReturn("Body");
        when(matchingTemplate.getRecipients()).thenReturn(List.of("guest@example.com"));

        MailNotificationBuilder builder = new MailNotificationBuilder(List.of(nonMatchingTemplate, matchingTemplate));
        Notification notification = builder.build(event);

        EmailNotification emailNotification = (EmailNotification) notification;
        assertEquals("Subject", emailNotification.getSubject());
        assertEquals("Body", emailNotification.getBody());
        assertEquals(List.of("guest@example.com"), emailNotification.getRecipients());
    }

    @Test
    void build_shouldThrow_whenNoTemplateMatches() {
        DomainEvent event = new ApplicationSharedEvent("app-1", "owner@example.com", "guest@example.com", "My App");
        when(nonMatchingTemplate.couldProcess(event)).thenReturn(false);

        MailNotificationBuilder builder = new MailNotificationBuilder(List.of(nonMatchingTemplate));

        assertThrows(NotificationException.class, () -> builder.build(event));
    }
}
