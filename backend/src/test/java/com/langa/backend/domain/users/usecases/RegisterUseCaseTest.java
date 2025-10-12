package com.langa.backend.domain.users.usecases;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.infra.rest.users.dto.RegisterRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUseCase registerUseCase;

    @Test
    void register_shouldSaveUser_whenDataIsValid() {
        RegisterRequestDto dto = new RegisterRequestDto("user@example.com", "password123", "password123");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        registerUseCase.register(dto.username(), dto.password(), dto.confirmationPassword());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_shouldThrowException_whenUsernameExists() {
        RegisterRequestDto dto = new RegisterRequestDto("user@example.com", "pwd", "pwd");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(User.createNew("user@example.com", "encodedPassword")));

        UserException ex = assertThrows(UserException.class, () -> registerUseCase.register(dto.username(), dto.password(), dto.confirmationPassword()));
        assertEquals(Errors.USERNAME_ALREADY_EXISTS, ex.getError());
    }

    @Test
    void register_shouldThrowException_whenPasswordsDoNotMatch() {
        RegisterRequestDto dto = new RegisterRequestDto("user@example.com", "pwd1", "pwd2");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        UserException ex = assertThrows(UserException.class, () -> registerUseCase.register(dto.username(), dto.password(), dto.confirmationPassword()));
        assertEquals(Errors.PASSWORDS_MISMATCH, ex.getError());
    }
}