package com.langa.backend.domain.teams.usecases;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.teams.TeamInvitation;
import com.langa.backend.domain.teams.exceptions.TeamException;
import com.langa.backend.domain.teams.repositories.TeamInvitationRepository;
import com.langa.backend.domain.teams.valueobjects.InvitationStatus;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationIdentity;
import com.langa.backend.domain.teams.valueobjects.TeamInvitationPeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetInvitationUseCaseTest {

    @InjectMocks
    private GetInvitationUseCase getInvitationUseCase;

    @Mock
    private TeamInvitationRepository teamInvitationRepository;

    @Test
    void shouldReturnInvitationByValidToken() {
        String validToken = "invitationToken123";
        TeamInvitation expectedInvitation = TeamInvitation.populate(new TeamInvitationIdentity("", validToken),
                null,
                new TeamInvitationPeriod(null, LocalDateTime.now().minusDays(1)), null, null);
        when(teamInvitationRepository.findByToken(validToken)).thenReturn(Optional.of(expectedInvitation));
        when(teamInvitationRepository.save(any())).thenReturn(expectedInvitation);

        TeamInvitation result = getInvitationUseCase.getInvitation(validToken);

        assertEquals(expectedInvitation, result);
    }

    @Test
    void shouldThrowExceptionIfInvitationExpired() {
        String expiredToken = "expiredToken";
        TeamInvitation expiredInvitation = TeamInvitation.populate(new TeamInvitationIdentity("", expiredToken), null,
                new TeamInvitationPeriod(null, LocalDateTime.now().minusDays(1)),
                null, null);

        when(teamInvitationRepository.findByToken(expiredToken)).thenReturn(Optional.of(expiredInvitation));
        when(teamInvitationRepository.save(any())).thenReturn(expiredInvitation);

        TeamInvitation result = getInvitationUseCase.getInvitation(expiredToken);

        assertEquals(InvitationStatus.EXPIRED, result.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenTokenIsInvalid() {
        String invalidToken = "invalidToken123";
        when(teamInvitationRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

        TeamException exception = assertThrows(TeamException.class, () -> getInvitationUseCase.getInvitation(invalidToken));
        assertEquals(Errors.TEAM_INVITATION_NOTFOUND_OR_EXPIRED, exception.getError());
    }
}