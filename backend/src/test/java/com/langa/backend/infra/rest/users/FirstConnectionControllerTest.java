package com.langa.backend.infra.rest.users;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.domain.users.usecases.fetch.IGetUserUseCase;
import com.langa.backend.domain.users.valueobjects.UserInfo;
import com.langa.backend.infra.rest.users.dto.CompleteFirstConnectionRequestDto;
import com.langa.backend.infra.rest.users.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirstConnectionControllerTest {

    @Mock
    private IGetUserUseCase getUserUseCase;
    @Mock
    private CommandBusDispatcher commandBusDispatcher;

    private FirstConnectionController controller() {
        return new FirstConnectionController(getUserUseCase, commandBusDispatcher);
    }

    @Test
    void getUserInfo_shouldReturnUserDto() {
        when(getUserUseCase.queryByFirstConnectionToken("token-1")).thenReturn(new UserInfo("user@example.com", "acc-1"));

        ResponseEntity<UserDto> response = controller().getUserInfo("token-1");

        assertEquals("user@example.com", response.getBody().email());
    }

    @Test
    void completeFirstConnectionProcess_shouldReturnAccepted() {
        when(commandBusDispatcher.dispatch(any())).thenReturn("First connection completed successfully");

        ResponseEntity<String> response = controller().completeFirstConnectionProcess(
                new CompleteFirstConnectionRequestDto("token-1", "Password1", "Password1"));

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("Account setup completed", response.getBody());
    }
}
