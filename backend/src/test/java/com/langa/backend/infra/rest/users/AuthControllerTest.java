package com.langa.backend.infra.rest.users;

import com.langa.backend.domain.users.usecases.LoginUseCase;
import com.langa.backend.domain.users.usecases.RegisterUseCase;
import com.langa.backend.domain.users.valueobjects.AuthToken;
import com.langa.backend.domain.users.valueobjects.AuthTokens;
import com.langa.backend.infra.rest.users.dto.LoginRequestDto;
import com.langa.backend.infra.rest.users.dto.LoginResponseDto;
import com.langa.backend.infra.rest.users.dto.RegisterRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private LoginUseCase loginUseCase;

    @Mock
    private RegisterUseCase registerUseCase;

    @InjectMocks
    private AuthController authController;


    @Test
    void register_shouldReturnOk() {
        RegisterRequestDto dto = new RegisterRequestDto("test@example.com", "password", "password");

        doNothing().when(registerUseCase).register(dto.username(), dto.password(), dto.confirmationPassword());

        ResponseEntity<String> response = authController.register(dto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered", response.getBody());
        verify(registerUseCase, times(1)).register(dto.username(), dto.password(), dto.confirmationPassword());
    }

    @Test
    void login_shouldReturnToken() {
        LoginRequestDto dto = new LoginRequestDto("test@example.com", "password");
        AuthTokens authToken = new AuthTokens("fake-token", "fake-refresh-token");

        when(loginUseCase.login(dto.toAuthRequest())).thenReturn(authToken);

        ResponseEntity<LoginResponseDto> response = authController.login(dto);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("fake-token", response.getBody().accessToken());
        verify(loginUseCase, times(1)).login(dto.toAuthRequest());
    }
}