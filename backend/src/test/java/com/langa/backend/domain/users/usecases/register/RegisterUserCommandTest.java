package com.langa.backend.domain.users.usecases.register;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.exceptions.UserException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegisterUserCommandTest {

    @Test
    void shouldAccept_validPassword() {
        assertDoesNotThrow(() -> new RegisterUserCommand("user@example.com", "Password1", "Password1"));
    }

    @Test
    void shouldReject_blankUsername() {
        assertThrows(UserException.class, () -> new RegisterUserCommand("", "Password1", "Password1"));
    }

    @Test
    void shouldReject_blankPassword() {
        assertThrows(UserException.class, () -> new RegisterUserCommand("user@example.com", "", "Password1"));
    }

    @Test
    void shouldReject_blankConfirmation() {
        assertThrows(UserException.class, () -> new RegisterUserCommand("user@example.com", "Password1", ""));
    }

    @Test
    void shouldReject_passwordTooShort() {
        UserException ex = assertThrows(UserException.class,
                () -> new RegisterUserCommand("user@example.com", "Pw1", "Pw1"));
        assertEquals(Errors.VALIDATION_ERROR, ex.getError());
    }

    @Test
    void shouldReject_passwordWithoutDigit() {
        assertThrows(UserException.class, () -> new RegisterUserCommand("user@example.com", "PasswordOnly", "PasswordOnly"));
    }

    @Test
    void shouldReject_passwordWithoutLetter() {
        assertThrows(UserException.class, () -> new RegisterUserCommand("user@example.com", "12345678", "12345678"));
    }

    @Test
    void shouldReject_whenPasswordsDoNotMatch() {
        UserException ex = assertThrows(UserException.class,
                () -> new RegisterUserCommand("user@example.com", "Password1", "Password2"));
        assertEquals(Errors.PASSWORDS_MISMATCH, ex.getError());
    }
}
