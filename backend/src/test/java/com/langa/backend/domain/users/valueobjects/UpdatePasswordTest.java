package com.langa.backend.domain.users.valueobjects;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.exceptions.UserException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdatePasswordTest {

    @Test
    void shouldAccept_matchingPasswords() {
        assertDoesNotThrow(() -> new UpdatePassword("Password1", "Password1"));
    }

    @Test
    void shouldReject_blankPassword() {
        assertThrows(UserException.class, () -> new UpdatePassword("", "Password1"));
    }

    @Test
    void shouldReject_blankConfirmation() {
        assertThrows(UserException.class, () -> new UpdatePassword("Password1", ""));
    }

    @Test
    void shouldReject_whenPasswordsDoNotMatch() {
        UserException ex = assertThrows(UserException.class, () -> new UpdatePassword("Password1", "Password2"));
        assertEquals(Errors.PASSWORDS_MISMATCH, ex.getError());
    }
}
