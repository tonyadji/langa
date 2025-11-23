package com.langa.backend.domain.users.usecases;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

@UseCase
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(String username, String password, String confirmationPassword) {
        if (userRepository.findByEmail(username).isPresent()) {
            throw new UserException("Username already exists", null, Errors.USERNAME_ALREADY_EXISTS);
        }

        if(!Objects.equals(password, confirmationPassword)) {
            throw new UserException("Password do not match", null, Errors.PASSWORDS_MISMATCH);
        }

        User user = User.createActive(username, passwordEncoder.encode(password));
        userRepository.save(user);
    }
}
