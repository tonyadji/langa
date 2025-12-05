package com.langa.backend.domain.users.usecases.register;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.PasswordService;
import org.springframework.security.crypto.password.PasswordEncoder;

@UseCase
public class RegisterUseCase implements IRegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public RegisterUseCase(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Override
    public void register(RegisterUserCommand command) {
        if (userRepository.findByEmail(command.username()).isPresent()) {
            throw new UserException("Username already exists", null, Errors.USERNAME_ALREADY_EXISTS);
        }

        User user = User.createActive(command.username(), passwordService.encode(command.password()));
        userRepository.save(user);
    }
}
