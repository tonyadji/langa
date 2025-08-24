package com.langa.backend.infra.rest.users;

import com.langa.backend.domain.users.usecases.GetUserUseCase;
import com.langa.backend.domain.users.valueobjects.UserInfo;
import com.langa.backend.infra.rest.users.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersControllerTest {

    @Mock
    private GetUserUseCase getUserUseCase;

    @InjectMocks
    private UsersController usersController;

    @Test
    void me_shouldReturnUserDto() {
        String username = "test@example.com";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(username);

        UserInfo userInfo = new UserInfo(username, "account-key-123");
        when(getUserUseCase.me(username)).thenReturn(userInfo);

        ResponseEntity<UserDto> response = usersController.me(userDetails);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(username, response.getBody().email());
        assertEquals("account-key-123", response.getBody().accountKey());
        verify(getUserUseCase, times(1)).me(username);
    }
}