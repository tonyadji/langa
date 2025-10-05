package com.langa.backend.application.listeners;

import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.events.InvitationAcceptedMailEvent;
import com.langa.backend.domain.teams.events.TeamInvitationAcceptedByGuestEvent;
import com.langa.backend.domain.teams.services.TeamMemberShipService;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.events.FirstConnectionMailEvent;
import com.langa.backend.domain.users.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TeamInvitationAcceptedByGuestListener {

    private final UserService userService;
    private final TeamMemberShipService teamMemberShipService;
    private final OutboxEventService outboxEventService;


    public TeamInvitationAcceptedByGuestListener(UserService userService,
                                                 TeamMemberShipService teamMemberShipService,
                                                 OutboxEventService outboxEventService) {

        this.userService = userService;
        this.teamMemberShipService = teamMemberShipService;
        this.outboxEventService = outboxEventService;
    }

    @Async
    @EventListener
    void handleTeamInvitationAcceptedByGuestEvent(TeamInvitationAcceptedByGuestEvent teamInvitationAcceptedByGuestEvent) {
        log.debug("Event received: {}", teamInvitationAcceptedByGuestEvent);
        User user = userService.findOrCreateUserByEmail(teamInvitationAcceptedByGuestEvent.guest());
        Team team = teamMemberShipService.addMemberToTeam(teamInvitationAcceptedByGuestEvent.team(), user.getEmail());

        outboxEventService.storeOutboxEvent(InvitationAcceptedMailEvent.of(team, user.getEmail()));
        outboxEventService.storeOutboxEvent(FirstConnectionMailEvent.of(user));
    }
}
