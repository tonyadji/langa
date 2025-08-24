package com.langa.backend.domain.users.usecases;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.valueobjects.UserInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class GetUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserUseCase getUserUseCase;

    @Test
    void me_shouldReturnUserInfo_whenUserExists() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setAccountKey("accountKey123");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UserInfo info = getUserUseCase.me("user@example.com");

        assertEquals("user@example.com", info.email());
        assertEquals("accountKey123", info.accountKey());
    }

    @Test
    void me_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        UserException ex = assertThrows(UserException.class,
                () -> getUserUseCase.me("unknown@example.com"));

        assertEquals(Errors.USER_NOT_FOUND, ex.getError());
    }
}