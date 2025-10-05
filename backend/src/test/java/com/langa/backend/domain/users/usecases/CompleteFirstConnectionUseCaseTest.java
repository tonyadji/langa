package com.langa.backend.domain.users.usecases;

import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.PasswordService;
import com.langa.backend.domain.users.valueobjects.UpdatePassword;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompleteFirstConnectionUseCaseTest {

    UserRepository userRepository = mock(UserRepository.class);
    PasswordService passwordService = mock(PasswordService.class);
    OutboxEventService outboxEventService = mock(OutboxEventService.class);

    @InjectMocks
    CompleteFirstConnectionUseCase useCase;

    @Test
    void shouldCompleteSuccessfullyGivenValidTokenAndPassword() {
        String firstConnectionToken = "valid-token";
        UpdatePassword updatePassword = new UpdatePassword("SecurePassword123!", "SecurePassword123!");
        when(userRepository.findByFistConnectionToken(firstConnectionToken)).thenReturn(Optional.of(new User()));
        when(passwordService.checkAndGetEncoded(updatePassword)).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(Optional.of(new User()));

        assertDoesNotThrow(() -> useCase.complete(firstConnectionToken, updatePassword));

        verify(passwordService, times(1)).checkAndGetEncoded(updatePassword);
        verify(userRepository, times(1)).save(any());
        verify(outboxEventService, times(1)).storeOutboxEvent(any());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        String firstConnectionToken = "first-connection-token";
        final UpdatePassword updatePassword =  new UpdatePassword("Password1!", "Password1!");
        when(userRepository.findByFistConnectionToken(firstConnectionToken)).thenReturn(Optional.empty());

        final UserException exception = assertThrows(UserException.class,
                () -> useCase.complete(firstConnectionToken, updatePassword));

        assertEquals("User not found", exception.getMessage());
        verifyNoInteractions(passwordService, outboxEventService);
    }

    @Test
    void shouldThrowExceptionWhenPasswordMismatch() {
        String firstConnectionToken = "first-connection-token";
        final UpdatePassword updatePassword =  new UpdatePassword("Password1!", "Password2!");
        when(userRepository.findByFistConnectionToken(firstConnectionToken)).thenReturn(Optional.of(new User()));
        when(passwordService.checkAndGetEncoded(updatePassword)).thenThrow(new UserException("Illegal password", null, Errors.PASSWORDS_MISMATCH));

        final UserException exception = assertThrows(UserException.class,
                () -> useCase.complete(firstConnectionToken, updatePassword));

        assertEquals("Illegal password", exception.getMessage());
        verifyNoInteractions(outboxEventService);
    }
}