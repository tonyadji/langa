package com.langa.backend.domain.users.usecases.firstconnection;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.eda.EventPublishingUseCase;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.PasswordService;

@UseCase
public class CompleteRegistrationProcessUseCase extends EventPublishingUseCase<User> {

    private final UserRepository userRepository;
    private final PasswordService passwordService;


    public CompleteRegistrationProcessUseCase(UserRepository userRepository,
                                              PasswordService passwordService,
                                              OutboxEventService outboxEventService) {
        super(outboxEventService);
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    public void execute(CompleteRegistrationProcessCommand command) {
        command.validate();
        final User user = userRepository.findByFistConnectionToken(command.firstConnectionToken())
                .orElseThrow(() -> new UserException("User not found", null, Errors.USER_NOT_FOUND));

        final String encodedPassword = passwordService.checkAndGetEncoded(command.updatePassword());

        user.completeRegistrationProcess(encodedPassword);

        userRepository.save(user);
        handleDomainEvents(user);
    }
}
