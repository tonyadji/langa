package com.langa.backend.domain.users.usecases.fetch;

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
    private GetUserUseCase useCase;

    @Test
    void queryByUsername_shouldReturnUserInfo_whenFound() {
        User user = User.createActive("user@example.com", "encoded");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UserInfo info = useCase.queryByUsername("user@example.com");

        assertEquals("user@example.com", info.email());
        assertEquals(user.getAccountKey(), info.accountKey());
    }

    @Test
    void queryByUsername_shouldThrow_whenNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        UserException ex = assertThrows(UserException.class, () -> useCase.queryByUsername("unknown@example.com"));
        assertEquals(Errors.USER_NOT_FOUND, ex.getError());
    }

    @Test
    void queryByFirstConnectionToken_shouldReturnUserInfo_whenFound() {
        User user = User.createNew("user@example.com", "temp");
        when(userRepository.findByFistConnectionToken("token-1")).thenReturn(Optional.of(user));

        UserInfo info = useCase.queryByFirstConnectionToken("token-1");

        assertEquals("user@example.com", info.email());
    }

    @Test
    void queryByFirstConnectionToken_shouldThrow_whenNotFound() {
        when(userRepository.findByFistConnectionToken("unknown")).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> useCase.queryByFirstConnectionToken("unknown"));
    }
}
