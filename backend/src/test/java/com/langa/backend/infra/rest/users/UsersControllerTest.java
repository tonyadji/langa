package com.langa.backend.infra.rest.users;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.domain.users.usecases.fetch.IGetUserUseCase;
import com.langa.backend.domain.users.valueobjects.UserInfo;
import com.langa.backend.infra.rest.users.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersControllerTest {

    @Mock
    private IGetUserUseCase getUserUseCase;
    @Mock
    private CommandBusDispatcher commandBusDispatcher;

    private final UserDetails userDetails = new User("user@example.com", "pw", List.of());

    private UsersController controller() {
        return new UsersController(getUserUseCase, commandBusDispatcher);
    }

    @Test
    void me_shouldReturnUserDto() {
        when(getUserUseCase.queryByUsername("user@example.com")).thenReturn(new UserInfo("user@example.com", "acc-1"));

        ResponseEntity<UserDto> response = controller().me(userDetails);

        assertEquals("user@example.com", response.getBody().email());
    }

    @Test
    void logout_shouldDispatchLogoutCommand() {
        ResponseEntity<String> response = controller().logout(userDetails);

        verify(commandBusDispatcher).dispatch(any());
        assertEquals("User Logged out", response.getBody());
    }
}
