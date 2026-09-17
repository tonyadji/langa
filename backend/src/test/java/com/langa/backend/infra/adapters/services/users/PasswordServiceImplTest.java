package com.langa.backend.infra.adapters.services.users;

import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.valueobjects.UpdatePassword;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordServiceImpl passwordService;

    @Test
    void checkAndGetEncoded_shouldReturnEncodedPassword_whenValidAndMatching() {
        when(passwordEncoder.encode("Password1")).thenReturn("encoded-password");

        String result = passwordService.checkAndGetEncoded(new UpdatePassword("Password1", "Password1"));

        assertEquals("encoded-password", result);
    }

    @Test
    void encode_shouldDelegateToPasswordEncoder() {
        when(passwordEncoder.encode("raw")).thenReturn("encoded");

        assertEquals("encoded", passwordService.encode("raw"));
    }
}
