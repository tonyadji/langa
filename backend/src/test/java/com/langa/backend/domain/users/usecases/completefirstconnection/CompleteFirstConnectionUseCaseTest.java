package com.langa.backend.domain.users.usecases.completefirstconnection;

import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.PasswordService;
import com.langa.backend.domain.users.valueobjects.UpdatePassword;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompleteFirstConnectionUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordService passwordService;
    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private CompleteFirstConnectionUseCase useCase;

    @Test
    void execute_shouldCompleteFirstConnection_andStoreEvent() {
        User user = User.createNew("user@example.com", "temp-password");
        when(userRepository.findByFistConnectionToken("token-1")).thenReturn(Optional.of(user));
        when(passwordService.checkAndGetEncoded(any(UpdatePassword.class))).thenReturn("encoded-password");

        String result = useCase.execute(new CompleteFirstConnectionCommand("token-1", new UpdatePassword("Password1", "Password1")));

        assertEquals("First connection completed successfully", result);
        assertEquals("encoded-password", user.getPassword());
        verify(outboxEventService, times(1)).storeOutboxEvent(any());
    }

    @Test
    void execute_shouldThrow_whenTokenNotFound() {
        when(userRepository.findByFistConnectionToken("unknown")).thenReturn(Optional.empty());

        UserException ex = assertThrows(UserException.class, () -> useCase.execute(
                new CompleteFirstConnectionCommand("unknown", new UpdatePassword("Password1", "Password1"))));

        assertEquals(Errors.USER_NOT_FOUND, ex.getError());
    }

    @Test
    void handle_shouldDelegateToExecute() {
        User user = User.createNew("user@example.com", "temp-password");
        when(userRepository.findByFistConnectionToken("token-1")).thenReturn(Optional.of(user));
        when(passwordService.checkAndGetEncoded(any(UpdatePassword.class))).thenReturn("encoded-password");

        String result = useCase.handle(new CompleteFirstConnectionCommand("token-1", new UpdatePassword("Password1", "Password1")));

        assertEquals("First connection completed successfully", result);
    }
}
