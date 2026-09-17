package com.langa.backend.domain.users.usecases.refreshtoken;

import com.langa.backend.domain.users.exceptions.UserException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RefreshAccessTokenCommandTest {

    @Test
    void shouldReject_blankRefreshToken() {
        assertThrows(UserException.class, () -> new RefreshAccessTokenCommand(""));
    }

    @Test
    void shouldReject_nullRefreshToken() {
        assertThrows(UserException.class, () -> new RefreshAccessTokenCommand(null));
    }
}
