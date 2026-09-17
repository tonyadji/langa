package com.langa.backend.infra.adapters.persistence.teams;

import com.langa.backend.domain.teams.valueobjects.InvitationStatus;
import com.langa.backend.domain.teams.valueobjects.TeamInvitation;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationIdentity;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationPeriod;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationStakeHolders;
import com.langa.backend.infra.adapters.persistence.teams.mongo.dao.TeamInvitationDao;
import com.langa.backend.infra.adapters.persistence.teams.mongo.documents.TeamInvitationDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamInvitationRepositoryImplTest {

    @Mock
    private TeamInvitationDao teamInvitationDao;

    @InjectMocks
    private TeamInvitationRepositoryImpl repository;

    private TeamInvitation invitation() {
        return TeamInvitation.populate(
                new TeamInvitationIdentity("team-1", "token-1"),
                new TeamInvitationStakeHolders("team-key", "host@example.com", "guest@example.com"),
                new TeamInvitationPeriod(LocalDateTime.now(), LocalDateTime.now().plusDays(1)),
                null, InvitationStatus.SENT);
    }

    @Test
    void save_shouldPersistAndReturnInvitation() {
        TeamInvitation invitation = invitation();
        when(teamInvitationDao.save(any(TeamInvitationDocument.class))).thenReturn(TeamInvitationDocument.of(invitation));

        TeamInvitation saved = repository.save(invitation);

        assertEquals("guest@example.com", saved.getStakeHolders().guest());
    }

    @Test
    void findExistingValidInvitation_shouldReturnInvitation() {
        TeamInvitation invitation = invitation();
        when(teamInvitationDao.findByTeamAndGuest("team-key", "guest@example.com"))
                .thenReturn(Optional.of(TeamInvitationDocument.of(invitation)));

        assertTrue(repository.findExistingValidInvitation("team-key", "guest@example.com").isPresent());
    }

    @Test
    void findByToken_shouldReturnInvitation() {
        TeamInvitation invitation = invitation();
        when(teamInvitationDao.findByInvitationToken("token-1")).thenReturn(Optional.of(TeamInvitationDocument.of(invitation)));

        assertTrue(repository.findByToken("token-1").isPresent());
    }

    @Test
    void findById_shouldReturnInvitation() {
        TeamInvitation invitation = invitation();
        when(teamInvitationDao.findById("id-1")).thenReturn(Optional.of(TeamInvitationDocument.of(invitation)));

        assertTrue(repository.findById("id-1").isPresent());
    }
}
