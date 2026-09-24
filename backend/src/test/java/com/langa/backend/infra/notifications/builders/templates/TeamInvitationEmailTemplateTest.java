package com.langa.backend.infra.notifications.builders.templates;

import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.domain.applications.events.ApplicationCreatedEvent;
import com.langa.backend.domain.teams.events.TeamInvitationEmailEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TeamInvitationEmailTemplateTest {

    private final TeamInvitationEmailTemplate template = new TeamInvitationEmailTemplate("http://localhost:3000");

    @Test
    void couldProcess_shouldMatchOnlyItsOwnEventType() {
        TeamInvitationEmailEvent event = new TeamInvitationEmailEvent(
                "team-1", "guest@example.com", "host@example.com", "Dev Team", "team-key", "token-1", LocalDateTime.now());
        DomainEvent other = ApplicationCreatedEvent.of(com.langa.backend.domain.applications.Application.createNew("App", "acc", "owner@example.com"));

        assertTrue(template.couldProcess(event));
        assertFalse(template.couldProcess(other));
    }

    @Test
    void processEvent_shouldBuildMessageAndRecipients() {
        TeamInvitationEmailEvent event = new TeamInvitationEmailEvent(
                "team-1", "guest@example.com", "host@example.com", "Dev Team", "team-key", "token-1", LocalDateTime.now());

        template.processEvent(event);

        assertEquals("TEAM INVITATION", template.getSubject());
        assertTrue(template.getMessage().contains("Dev Team"));
        assertTrue(template.getMessage().contains("token-1"));
        assertEquals(1, template.getRecipients().size());
        assertEquals("guest@example.com", template.getRecipients().get(0));
    }
}
