package com.langa.backend.domain.users;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void createActive_shouldInitializeFieldsCorrectly() {
        String email = "user@example.com";
        String password = "encodedPassword";

        User user = User.createActive(email, password);

        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertNotNull(user.getAccountKey(), "Account key should be generated");
    }
}