package com.langa.backend.domain.users.usecases.logout;

import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.services.TokenService;
import com.langa.backend.domain.users.valueobjects.AuthTokens;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private LogoutUseCase useCase;

    @Test
    void execute_shouldRevokeAllTokensForUser() {
        AuthTokens result = useCase.execute(new LogoutCommand("user@example.com"));

        verify(tokenService).revokeAllByUserEmail("user@example.com");
        assertNull(result.accessToken());
        assertNull(result.refreshToken());
    }

    @Test
    void handle_shouldDelegateToExecute() {
        AuthTokens result = useCase.handle(new LogoutCommand("user@example.com"));
        verify(tokenService).revokeAllByUserEmail("user@example.com");
        assertNotNull(result);
    }

    @Test
    void logoutCommand_shouldReject_blankUsername() {
        assertThrows(UserException.class, () -> new LogoutCommand(""));
    }
}
