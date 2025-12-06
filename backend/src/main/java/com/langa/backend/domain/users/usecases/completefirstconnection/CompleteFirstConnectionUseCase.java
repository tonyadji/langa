package com.langa.backend.domain.users.usecases.completefirstconnection;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.PasswordService;
import com.langa.backend.domain.users.valueobjects.UpdatePassword;

@UseCase
public class CompleteFirstConnectionUseCase implements ICompleteFirstConnection, CommandHandler<CompleteFirstConnectionCommand, String> {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final OutboxEventService outboxEventService;


    public CompleteFirstConnectionUseCase(UserRepository userRepository,
                                          PasswordService passwordService,
                                          OutboxEventService outboxEventService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.outboxEventService = outboxEventService;
    }

    @Override
    public String handle(CompleteFirstConnectionCommand command) {
        return execute(command);
    }

    @Override
    public String execute(CompleteFirstConnectionCommand command) {
        final User user = userRepository.findByFistConnectionToken(command.firstConnectionToken())
                .orElseThrow(() -> new UserException("User not found", null, Errors.USER_NOT_FOUND));

        final String encodedPassword = passwordService.checkAndGetEncoded(command.updatePassword());

        user.completeFirstConnection(encodedPassword);

        userRepository.save(user);

        handleDomainEvents(user);

        return "First connection completed successfully";
    }

    private void handleDomainEvents(User user) {
        user.getEvents().forEach(outboxEventService::storeOutboxEvent);
        user.clearEvents();
    }
}
