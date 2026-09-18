package com.langa.backend.domain.users.usecases.register;

import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.PasswordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordService passwordService;
    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private RegisterUseCase useCase;

    @Test
    void execute_shouldRegisterUser_andStoreOutboxEvent() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(passwordService.encode("Password1")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(Optional.empty());

        useCase.execute(new RegisterUserCommand("user@example.com", "Password1", "Password1"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("user@example.com", captor.getValue().getEmail());
        assertEquals("encoded-password", captor.getValue().getPassword());
        verify(outboxEventService, times(1)).storeOutboxEvent(any());
    }

    @Test
    void execute_shouldThrow_whenUsernameAlreadyExists() {
        User existing = User.createActive("user@example.com", "encoded");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existing));

        UserException ex = assertThrows(UserException.class,
                () -> useCase.execute(new RegisterUserCommand("user@example.com", "Password1", "Password1")));

        assertEquals(Errors.USERNAME_ALREADY_EXISTS, ex.getError());
        verify(userRepository, never()).save(any());
    }

    @Test
    void handle_shouldReturnConfirmationMessage() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(passwordService.encode("Password1")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(Optional.empty());

        String result = useCase.handle(new RegisterUserCommand("user@example.com", "Password1", "Password1"));

        assertEquals("User Registered", result);
    }
}
