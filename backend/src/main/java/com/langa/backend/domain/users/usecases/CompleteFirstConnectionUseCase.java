package com.langa.backend.domain.users.usecases;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.exceptions.UserException;
import com.langa.backend.domain.users.repositories.UserRepository;
import com.langa.backend.domain.users.services.PasswordService;
import com.langa.backend.domain.users.valueobjects.UpdatePassword;

@UseCase
public class CompleteFirstConnectionUseCase {

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

    public void complete(String firstConnectionToken, UpdatePassword updatePassword) {
        final User user = userRepository.findByFistConnectionToken(firstConnectionToken)
                .orElseThrow(() -> new UserException("User not found", null, Errors.USER_NOT_FOUND));

        final String encodedPassword = passwordService.checkAndGetEncoded(updatePassword);

        DomainEvent event = user.completeFirstConnection(encodedPassword);

        userRepository.save(user);

        outboxEventService.storeOutboxEvent(event);
    }
}
