package com.langa.backend.application.listeners;

import com.langa.backend.domain.teams.events.TeamInvitationEmailEvent;
import com.langa.backend.infra.notifications.NotificationService;
import com.langa.backend.infra.notifications.builders.MailNotificationBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TeamInvitationEmailListener {

    private final NotificationService notificationService;
    private final MailNotificationBuilder mailNotificationBuilder;


    public TeamInvitationEmailListener(NotificationService notificationService, MailNotificationBuilder mailNotificationBuilder) {
        this.notificationService = notificationService;
        this.mailNotificationBuilder = mailNotificationBuilder;
    }

    @Async
    @EventListener
    void handleTeamInvitationEmailEvent(TeamInvitationEmailEvent teamInvitationEmailEvent) {
        log.debug("Event received: {}", teamInvitationEmailEvent);
        notificationService.send(mailNotificationBuilder.build(teamInvitationEmailEvent));
    }
}
