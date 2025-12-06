package com.langa.backend.domain.users.usecases.register;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.PasswordService;

import java.util.List;

@UseCase
public class RegisterUseCase implements IRegisterUseCase, CommandHandler<RegisterUserCommand, String> {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final OutboxEventService outboxEventService;

    public RegisterUseCase(UserRepository userRepository,
                           PasswordService passwordService,
                           OutboxEventService outboxEventService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.outboxEventService = outboxEventService;
    }

    @Override
    public void execute(RegisterUserCommand command) {
        if (userRepository.findByEmail(command.username()).isPresent()) {
            throw new UserException("Username already exists", null, Errors.USERNAME_ALREADY_EXISTS);
        }

        User user = User.createActive(command.username(), passwordService.encode(command.password()));
        userRepository.save(user);
        handleDomainEvents(user);
    }

    private void handleDomainEvents(User user) {
        user.getEvents().forEach(outboxEventService::storeOutboxEvent);

        user.clearEvents();
    }

    @Override
    public String handle(RegisterUserCommand command) {
        execute(command);
        return "User Registered";
    }
}
