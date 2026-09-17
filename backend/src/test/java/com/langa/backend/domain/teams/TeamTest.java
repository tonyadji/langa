package com.langa.backend.domain.teams;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.valueobjects.TeamInvitation;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationIdentity;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationPeriod;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationStakeHolders;
import com.langa.backend.domain.teams.valueobjects.InvitationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TeamTest {

    private Team team() {
        return Team.createNew("Dev Team", "owner@example.com", LocalDateTime.now());
    }

    @Test
    void createNew_shouldRegisterOwnerAsMember() {
        Team team = team();
        assertEquals(1, team.getMembers().size());
        assertEquals("owner@example.com", team.getMembers().get(0).email());
        assertNotNull(team.getKey());
    }

    @Test
    void checkOwnership_shouldPass_whenOwnerMatches() {
        Team team = team();
        assertDoesNotThrow(() -> team.checkOwnership("owner@example.com"));
    }

    @Test
    void checkOwnership_shouldThrow_whenNotOwner() {
        Team team = team();
        TeamException ex = assertThrows(TeamException.class, () -> team.checkOwnership("intruder@example.com"));
        assertEquals(Errors.ACCESS_DENIED, ex.getError());
    }

    @Test
    void checkMemberShip_shouldPass_forMember() {
        Team team = team();
        assertDoesNotThrow(() -> team.checkMemberShip("owner@example.com"));
    }

    @Test
    void checkMemberShip_shouldThrow_whenNotAMember() {
        Team team = team();
        TeamException ex = assertThrows(TeamException.class, () -> team.checkMemberShip("stranger@example.com"));
        assertEquals(Errors.TEAM_MEMBER_NOT_FOUND, ex.getError());
    }

    @Test
    void invite_shouldAddInvitation_andRegisterEvent() {
        Team team = team();
        team.invite("guest@example.com");

        assertEquals(1, team.getInvitations().size());
        assertEquals(1, team.getEvents().size());
    }

    @Test
    void invite_shouldThrow_whenGuestAlreadyMember() {
        Team team = team();
        TeamException ex = assertThrows(TeamException.class, () -> team.invite("owner@example.com"));
        assertEquals(Errors.TEAM_MEMBER_ALREADY, ex.getError());
    }

    @Test
    void invite_shouldThrow_whenAlreadyHasValidInvitation() {
        Team team = team();
        team.invite("guest@example.com");

        TeamException ex = assertThrows(TeamException.class, () -> team.invite("guest@example.com"));
        assertEquals(Errors.TEAM_INVITATION_EXISTING, ex.getError());
    }

    @Test
    void addMember_shouldAddNewMember() {
        Team team = team();
        team.addMember("member@example.com");

        assertEquals(2, team.getMembers().size());
        assertEquals(1, team.getNewMembers().size());
    }

    @Test
    void addMember_shouldThrow_whenAlreadyMember() {
        Team team = team();
        TeamException ex = assertThrows(TeamException.class, () -> team.addMember("owner@example.com"));
        assertEquals(Errors.TEAM_MEMBER_ALREADY, ex.getError());
    }

    @Test
    void getInvitation_shouldReturnNull_whenTokenUnknown() {
        Team team = team();
        assertNull(team.getInvitation("unknown-token"));
    }

    @Test
    void getInvitation_shouldReturnMatchingInvitation() {
        Team team = team();
        team.invite("guest@example.com");
        String token = team.getInvitations().get(0).getToken();

        assertNotNull(team.getInvitation(token));
    }

    @Test
    void acceptInvitation_shouldReturnFalse_whenInvitationIsNull() {
        Team team = team();
        assertFalse(team.acceptInvitation(null));
    }

    @Test
    void acceptInvitation_shouldAccept_whenNotExpired() {
        Team team = team();
        team.invite("guest@example.com");
        TeamInvitation invitation = team.getInvitations().get(0);

        boolean accepted = team.acceptInvitation(invitation);

        assertTrue(accepted);
        assertEquals(InvitationStatus.ACCEPTED, team.getInvitation(invitation.getToken()).getStatus());
        // 1 event from invite() + 2 from acceptInvitation()
        assertEquals(3, team.getEvents().size());
    }

    @Test
    void acceptInvitation_shouldMarkExpired_whenPeriodElapsed() {
        Team team = team();
        TeamInvitation expiredInvitation = TeamInvitation.populate(
                new TeamInvitationIdentity(team.getId(), "token-1"),
                new TeamInvitationStakeHolders(team.getKey(), "owner@example.com", "guest@example.com"),
                new TeamInvitationPeriod(LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1)),
                null, InvitationStatus.SENT);

        boolean accepted = team.acceptInvitation(expiredInvitation);

        assertFalse(accepted);
    }

    @Test
    void flagInvitationExpired_shouldMarkInvitationAsExpired() {
        Team team = team();
        team.invite("guest@example.com");
        TeamInvitation invitation = team.getInvitations().get(0);

        team.flagInvitationExpired(invitation);

        assertEquals(InvitationStatus.EXPIRED, team.getInvitation(invitation.getToken()).getStatus());
    }
}
