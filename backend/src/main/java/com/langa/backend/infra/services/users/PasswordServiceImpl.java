package com.langa.backend.infra.services.users;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.services.PasswordService;
import com.langa.backend.domain.users.valueobjects.UpdatePassword;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PasswordServiceImpl implements PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public String checkAndGetEncoded(UpdatePassword updatePassword) {
        if(isLegalPassword(updatePassword.password()) && Objects.equals(updatePassword.password(), updatePassword.confirmationPassword())) {
            return passwordEncoder.encode(updatePassword.password());
        }
        throw new UserException("Illegal password", null, Errors.PASSWORDS_MISMATCH);
    }

    private boolean isLegalPassword(String password) {
        return !(Objects.isNull(password) || password.isBlank());
    }
}
