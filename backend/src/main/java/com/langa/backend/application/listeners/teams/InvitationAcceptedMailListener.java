package com.langa.backend.application.listeners.teams;

import com.langa.backend.domain.teams.events.InvitationAcceptedMailEvent;
import com.langa.backend.infra.notifications.NotificationService;
import com.langa.backend.infra.notifications.builders.MailNotificationBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InvitationAcceptedMailListener {

    private final NotificationService notificationService;
    private final MailNotificationBuilder mailNotificationBuilder;


    public InvitationAcceptedMailListener(NotificationService notificationService, MailNotificationBuilder mailNotificationBuilder) {
        this.notificationService = notificationService;
        this.mailNotificationBuilder = mailNotificationBuilder;
    }

    @Async
    @EventListener
    void handleTeamInvitationEmailEvent(InvitationAcceptedMailEvent invitationAcceptedMailEvent) {
        log.debug("Event received: {}", invitationAcceptedMailEvent);
        try {
            notificationService.send(mailNotificationBuilder.build(invitationAcceptedMailEvent));
        } catch (Exception e) {
            log.error("Error handling event: {}", invitationAcceptedMailEvent, e);
        }

    }
}
