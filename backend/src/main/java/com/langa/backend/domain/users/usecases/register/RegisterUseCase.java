package com.langa.backend.domain.users.usecases.register;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.eda.EventPublishingUseCase;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.PasswordService;

@UseCase
public class RegisterUseCase extends EventPublishingUseCase<User> {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public RegisterUseCase(UserRepository userRepository, PasswordService passwordService,
                           OutboxEventService outboxEventService) {
        super(outboxEventService);
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    public void execute(RegisterCommand command) {
        command.validate();
        if (userRepository.findByEmail(command.username()).isPresent()) {
            throw new UserException("Username already exists", null, Errors.USERNAME_ALREADY_EXISTS);
        }

        User user = User.createActive(command.username(), passwordService.encode(command.password()));
        userRepository.save(user);
        handleDomainEvents(user);
    }
}
