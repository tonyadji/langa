package com.langa.backend.domain.teams.usecases;

import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.usecases.create.CreateTeamCommand;
import com.langa.backend.domain.teams.usecases.invitations.accept.AcceptInvitationCommand;
import com.langa.backend.domain.teams.usecases.invitations.send.SendInvitationCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TeamCommandsValidationTest {

    @Test
    void createTeamCommand_shouldRejectBlankNameOrOwner() {
        assertThrows(TeamException.class, () -> new CreateTeamCommand("", "owner@example.com"));
        assertThrows(TeamException.class, () -> new CreateTeamCommand("name", ""));
    }

    @Test
    void acceptInvitationCommand_shouldRejectMissingFields() {
        assertThrows(TeamException.class, () -> new AcceptInvitationCommand(" ", "token", "guest@example.com"));
        assertThrows(TeamException.class, () -> new AcceptInvitationCommand("team-1", " ", "guest@example.com"));
        assertThrows(TeamException.class, () -> new AcceptInvitationCommand("team-1", "token", " "));
    }

    @Test
    void sendInvitationCommand_shouldRejectMissingFields() {
        assertThrows(TeamException.class, () -> new SendInvitationCommand(" ", "team-1", "host@example.com"));
        assertThrows(TeamException.class, () -> new SendInvitationCommand("guest@example.com", " ", "host@example.com"));
        assertThrows(TeamException.class, () -> new SendInvitationCommand("guest@example.com", "team-1", " "));
    }
}
