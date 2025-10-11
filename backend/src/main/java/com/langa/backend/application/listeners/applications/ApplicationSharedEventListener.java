package com.langa.backend.application.listeners.applications;

import com.langa.backend.domain.applications.events.ApplicationSharedEvent;
import com.langa.backend.infra.notifications.NotificationService;
import com.langa.backend.infra.notifications.builders.MailNotificationBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationSharedEventListener {

    private final NotificationService notificationService;
    private final MailNotificationBuilder mailNotificationBuilder;


    public ApplicationSharedEventListener(NotificationService notificationService, MailNotificationBuilder mailNotificationBuilder) {
        this.notificationService = notificationService;
        this.mailNotificationBuilder = mailNotificationBuilder;
    }

    @Async
    @EventListener
    void handleTeamInvitationEmailEvent(ApplicationSharedEvent applicationSharedEvent) {
        log.debug("Event received: {}", applicationSharedEvent);
        try {
            notificationService.send(mailNotificationBuilder.build(applicationSharedEvent));
        } catch (Exception e) {
            log.error("Error handling event: {}", applicationSharedEvent, e);
        }
    }
}
