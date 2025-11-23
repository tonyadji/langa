package com.langa.backend.application.listeners.users;

import com.langa.backend.domain.users.events.AccountSetupCompleteMailEvent;
import com.langa.backend.infra.notifications.NotificationService;
import com.langa.backend.infra.notifications.builders.MailNotificationBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountSetupCompleteMailListener {

    private final NotificationService notificationService;
    private final MailNotificationBuilder mailNotificationBuilder;


    public AccountSetupCompleteMailListener(NotificationService notificationService, MailNotificationBuilder mailNotificationBuilder) {
        this.notificationService = notificationService;
        this.mailNotificationBuilder = mailNotificationBuilder;
    }

    @Async
    @EventListener
    void handleTeamInvitationEmailEvent(AccountSetupCompleteMailEvent accountSetupCompleteMailEvent) {
        log.debug("Event received: {}", accountSetupCompleteMailEvent);
        try {
            notificationService.send(mailNotificationBuilder.build(accountSetupCompleteMailEvent));
        } catch (Exception e) {
            log.error("Error handling event: {}", accountSetupCompleteMailEvent, e);
        }
    }
}
