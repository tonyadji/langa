package com.langa.backend.application.listeners.users;

import com.langa.backend.domain.users.events.FirstConnectionMailEvent;
import com.langa.backend.infra.notifications.NotificationService;
import com.langa.backend.infra.notifications.builders.MailNotificationBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FirstConnectionMailListener {

    private final NotificationService notificationService;
    private final MailNotificationBuilder mailNotificationBuilder;


    public FirstConnectionMailListener(NotificationService notificationService, MailNotificationBuilder mailNotificationBuilder) {
        this.notificationService = notificationService;
        this.mailNotificationBuilder = mailNotificationBuilder;
    }

    @Async
    @EventListener
    void handleTeamInvitationEmailEvent(FirstConnectionMailEvent firstConnectionMailEvent) {
        log.debug("Event received: {}", firstConnectionMailEvent);
        notificationService.send(mailNotificationBuilder.build(firstConnectionMailEvent));
    }
}
