package com.langa.backend.domain.events;

import com.langa.backend.domain.users.valueobjects.ExternalIdentity;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.events.ApplicationCreatedEvent;
import com.langa.backend.domain.applications.events.ApplicationSharedEvent;
import com.langa.backend.domain.teams.Team;
import com.langa.backend.domain.teams.events.InvitationAcceptedMailEvent;
import com.langa.backend.domain.teams.events.TeamInvitationAcceptedByGuestEvent;
import com.langa.backend.domain.teams.events.TeamInvitationAcceptedForHostEvent;
import com.langa.backend.domain.teams.events.TeamInvitationEmailEvent;
import com.langa.backend.domain.teams.valueobjects.TeamInvitation;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.events.ActiveUserRegisteredEvent;
import com.langa.backend.domain.users.events.FirstConnectionMailEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DomainEventsTest {

    @Test
    void applicationCreatedEvent_shouldExposeAggregateInfo() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        ApplicationCreatedEvent event = ApplicationCreatedEvent.of(app);

        assertEquals(app.getId(), event.getAggregateId());
        assertEquals("Application", event.getAggregateType());
        assertNotNull(event.getEventType());
    }

    @Test
    void applicationSharedEvent_shouldExposeAggregateInfo() {
        ApplicationSharedEvent event = new ApplicationSharedEvent("app-1", "owner@example.com", "guest@example.com", "My App");

        assertEquals("app-1", event.getAggregateId());
        assertEquals("Application", event.getAggregateType());
        assertNotNull(event.getEventType());
    }

    @Test
    void activeUserRegisteredEvent_shouldExposeAggregateInfo() {
        User user = User.createFromExternalIdentity(new ExternalIdentity("entra", "oid-1", "user@example.com"));
        ActiveUserRegisteredEvent event = ActiveUserRegisteredEvent.of(user);

        assertEquals(user.getId(), event.getAggregateId());
        assertEquals("User", event.getAggregateType());
        assertNotNull(event.getEventType());
    }

    @Test
    void firstConnectionMailEvent_shouldExposeAggregateInfo() {
        User user = User.createInvited("user@example.com");
        FirstConnectionMailEvent event = FirstConnectionMailEvent.of(user);

        assertEquals(user.getAccountKey(), event.getAggregateId());
        assertEquals("User", event.getAggregateType());
        assertNotNull(event.getEventType());
    }

    @Test
    void teamInvitationEmailEvent_shouldExposeAggregateInfo() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        team.invite("guest@example.com");
        TeamInvitation invitation = team.getInvitations().get(0);
        TeamInvitationEmailEvent event = TeamInvitationEmailEvent.of(invitation, team);

        assertEquals(team.getId(), event.getAggregateId());
        assertEquals("TeamInvitation", event.getAggregateType());
        assertNotNull(event.getEventType());
    }

    @Test
    void teamInvitationAcceptedByGuestEvent_shouldExposeAggregateInfo() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        team.invite("guest@example.com");
        TeamInvitation invitation = team.getInvitations().get(0);
        TeamInvitationAcceptedByGuestEvent event = TeamInvitationAcceptedByGuestEvent.of(invitation);

        assertEquals(team.getId(), event.getAggregateId());
        assertEquals("TeamInvitation", event.getAggregateType());
        assertNotNull(event.getEventType());
    }

    @Test
    void teamInvitationAcceptedForHostEvent_shouldExposeAggregateInfo() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        team.invite("guest@example.com");
        TeamInvitation invitation = team.getInvitations().get(0);
        TeamInvitationAcceptedForHostEvent event = TeamInvitationAcceptedForHostEvent.of(invitation);

        assertEquals(team.getId(), event.getAggregateId());
        assertEquals("TeamInvitation", event.getAggregateType());
        assertNotNull(event.getEventType());
    }

    @Test
    void invitationAcceptedMailEvent_shouldExposeAggregateInfo() {
        Team team = Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
        InvitationAcceptedMailEvent event = InvitationAcceptedMailEvent.of(team, "guest@example.com");

        assertEquals(team.getId(), event.getAggregateId());
        assertEquals("Team", event.getAggregateType());
        assertNotNull(event.getEventType());
    }
}
