package com.langa.backend.domain.teams.valueobjects;

import com.langa.backend.domain.teams.exceptions.TeamException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TeamInvitationTest {

    private TeamInvitation invitation(InvitationStatus status, LocalDateTime expiry) {
        return TeamInvitation.populate(
                new TeamInvitationIdentity("team-1", "token-1"),
                new TeamInvitationStakeHolders("team-key", "host@example.com", "guest@example.com"),
                new TeamInvitationPeriod(LocalDateTime.now(), expiry),
                null, status);
    }

    @Test
    void accept_shouldSetAcceptedStatus() {
        TeamInvitation invitation = invitation(InvitationStatus.SENT, LocalDateTime.now().plusDays(1));

        TeamInvitation accepted = invitation.accept();

        assertEquals(InvitationStatus.ACCEPTED, accepted.getStatus());
        assertNotNull(accepted.getAcceptedDate());
    }

    @Test
    void accept_shouldThrow_whenAlreadyAccepted() {
        TeamInvitation invitation = invitation(InvitationStatus.ACCEPTED, LocalDateTime.now().plusDays(1));
        assertThrows(TeamException.class, invitation::accept);
    }

    @Test
    void accept_shouldThrow_whenExpiredStatus() {
        TeamInvitation invitation = invitation(InvitationStatus.EXPIRED, LocalDateTime.now().plusDays(1));
        assertThrows(TeamException.class, invitation::accept);
    }

    @Test
    void isExpired_shouldReturnTrue_whenPeriodElapsed() {
        TeamInvitation invitation = invitation(InvitationStatus.SENT, LocalDateTime.now().minusMinutes(1));
        assertTrue(invitation.isExpired());
    }

    @Test
    void isExpired_shouldReturnFalse_whenWithinPeriod() {
        TeamInvitation invitation = invitation(InvitationStatus.SENT, LocalDateTime.now().plusDays(1));
        assertFalse(invitation.isExpired());
    }

    @Test
    void markAsExpired_shouldSetExpiredStatus() {
        TeamInvitation invitation = invitation(InvitationStatus.SENT, LocalDateTime.now().plusDays(1));
        invitation.markAsExpired();
        assertEquals(InvitationStatus.EXPIRED, invitation.getStatus());
    }

    @Test
    void isVisibleBy_shouldReturnTrue_forGuestOrHost() {
        TeamInvitation invitation = invitation(InvitationStatus.SENT, LocalDateTime.now().plusDays(1));
        assertTrue(invitation.isVisibleBy("guest@example.com"));
        assertTrue(invitation.isVisibleBy("host@example.com"));
        assertFalse(invitation.isVisibleBy("stranger@example.com"));
    }

    @Test
    void canAccept_shouldReturnTrue_onlyForGuest() {
        TeamInvitation invitation = invitation(InvitationStatus.SENT, LocalDateTime.now().plusDays(1));
        assertTrue(invitation.canAccept("guest@example.com"));
        assertFalse(invitation.canAccept("host@example.com"));
    }

    @Test
    void getTeamId_andGetToken_shouldDelegateToIdentity() {
        TeamInvitation invitation = invitation(InvitationStatus.SENT, LocalDateTime.now().plusDays(1));
        assertEquals("team-1", invitation.getTeamId());
        assertEquals("token-1", invitation.getToken());
    }
}
