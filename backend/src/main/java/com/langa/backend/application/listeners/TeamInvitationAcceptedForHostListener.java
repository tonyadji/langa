package com.langa.backend.application.listeners;

import com.langa.backend.domain.teams.events.TeamInvitationAcceptedForHostEvent;
import com.langa.backend.infra.notifications.NotificationService;
import com.langa.backend.infra.notifications.builders.MailNotificationBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TeamInvitationAcceptedForHostListener {

    private final NotificationService notificationService;
    private final MailNotificationBuilder mailNotificationBuilder;


    public TeamInvitationAcceptedForHostListener(NotificationService notificationService, MailNotificationBuilder mailNotificationBuilder) {
        this.notificationService = notificationService;
        this.mailNotificationBuilder = mailNotificationBuilder;
    }

    @Async
    @EventListener
    void handleTeamInvitationAcceptedForHostEvent(TeamInvitationAcceptedForHostEvent teamInvitationAcceptedForHostEvent) {
        log.debug("Event received: {}", teamInvitationAcceptedForHostEvent);
        notificationService.send(mailNotificationBuilder.build(teamInvitationAcceptedForHostEvent));
    }
}
