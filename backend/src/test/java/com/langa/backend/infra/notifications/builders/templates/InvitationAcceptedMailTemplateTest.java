package com.langa.backend.infra.notifications.builders.templates;

import com.langa.backend.domain.teams.events.InvitationAcceptedMailEvent;
import com.langa.backend.domain.users.events.ActiveUserRegisteredEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvitationAcceptedMailTemplateTest {

    private final InvitationAcceptedMailTemplate template = new InvitationAcceptedMailTemplate();

    @Test
    void couldProcess_shouldMatchOnlyItsOwnEventType() {
        InvitationAcceptedMailEvent event = new InvitationAcceptedMailEvent("team-1", "Dev Team", "member@example.com");
        ActiveUserRegisteredEvent other = new ActiveUserRegisteredEvent("user-1", "user@example.com");

        assertTrue(template.couldProcess(event));
        assertFalse(template.couldProcess(other));
    }

    @Test
    void processEvent_shouldBuildMessageAndRecipients() {
        InvitationAcceptedMailEvent event = new InvitationAcceptedMailEvent("team-1", "Dev Team", "member@example.com");

        template.processEvent(event);

        assertEquals("First Connection", template.getSubject());
        assertTrue(template.getMessage().contains("member@example.com"));
        assertEquals(List.of("member@example.com"), template.getRecipients());
    }
}
