package com.langa.backend.infra.notifications.builders.templates;

import com.langa.backend.domain.teams.events.TeamInvitationAcceptedForHostEvent;
import com.langa.backend.domain.users.events.ActiveUserRegisteredEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeamInvitationAcceptedForHostEmailTemplateTest {

    private final TeamInvitationAcceptedForHostEmailTemplate template = new TeamInvitationAcceptedForHostEmailTemplate();

    @Test
    void couldProcess_shouldMatchOnlyItsOwnEventType() {
        TeamInvitationAcceptedForHostEvent event = new TeamInvitationAcceptedForHostEvent(
                "team-1", "guest@example.com", "host@example.com", "team-key", "token-1", LocalDateTime.now());
        ActiveUserRegisteredEvent other = new ActiveUserRegisteredEvent("user-1", "user@example.com");

        assertTrue(template.couldProcess(event));
        assertFalse(template.couldProcess(other));
    }

    @Test
    void processEvent_shouldBuildMessageAndRecipients() {
        TeamInvitationAcceptedForHostEvent event = new TeamInvitationAcceptedForHostEvent(
                "team-1", "guest@example.com", "host@example.com", "team-key", "token-1", LocalDateTime.now());

        template.processEvent(event);

        assertEquals("INVITATION ACCEPTED", template.getSubject());
        assertTrue(template.getMessage().contains("guest@example.com"));
        assertEquals(List.of("host@example.com"), template.getRecipients());
    }
}
