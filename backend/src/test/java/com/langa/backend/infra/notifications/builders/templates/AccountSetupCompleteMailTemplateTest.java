package com.langa.backend.infra.notifications.builders.templates;

import com.langa.backend.domain.users.events.AccountSetupCompleteMailEvent;
import com.langa.backend.domain.teams.events.InvitationAcceptedMailEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountSetupCompleteMailTemplateTest {

    private final AccountSetupCompleteMailTemplate template = new AccountSetupCompleteMailTemplate("http://localhost:3000");

    @Test
    void couldProcess_shouldMatchOnlyItsOwnEventType() {
        AccountSetupCompleteMailEvent event = new AccountSetupCompleteMailEvent("user-1", "user@example.com");
        InvitationAcceptedMailEvent other = new InvitationAcceptedMailEvent("team-1", "Dev Team", "member@example.com");

        assertTrue(template.couldProcess(event));
        assertFalse(template.couldProcess(other));
    }

    @Test
    void processEvent_shouldBuildMessageAndRecipients() {
        AccountSetupCompleteMailEvent event = new AccountSetupCompleteMailEvent("user-1", "user@example.com");

        template.processEvent(event);

        assertEquals("Account Setup Complete", template.getSubject());
        assertTrue(template.getMessage().contains("user@example.com"));
        assertEquals(List.of("user@example.com"), template.getRecipients());
    }
}
