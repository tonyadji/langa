package com.langa.backend.domain.applications.usecases.create;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.domainexchange.user.UserAccountService;

@UseCase
public class CreateApplicationUseCase implements ICreateApplicationUseCase, CommandHandler<CreateApplicationCommand, ApplicationInfo> {

    private final ApplicationRepository applicationRepository;
    private final UserAccountService userAccountService;
    private final OutboxEventService outboxEventService;

    public CreateApplicationUseCase(ApplicationRepository applicationRepository,
                                    UserAccountService userAccountService, OutboxEventService outboxEventService) {
        this.applicationRepository = applicationRepository;
        this.userAccountService = userAccountService;
        this.outboxEventService = outboxEventService;
    }


    @Override
    public ApplicationInfo execute(CreateApplicationCommand command) {
        applicationRepository.findByOwnerAndName(command.ownerEmail(), command.name())
                .ifPresent(app -> {
                    throw new ApplicationException(
                            "Application name already exists",
                            null,
                            Errors.APPLICATION_NAME_ALREADY_EXISTS
                    );
                });

        String accountKey = userAccountService.getAccountKey(command.ownerEmail());

        final Application app = Application.createNew(command.name(), accountKey, command.ownerEmail());
        final ApplicationInfo applicationInfo = ApplicationInfo.of(applicationRepository.save(app));
        handleDomainEvents(app);
        return applicationInfo;
    }

    @Override
    public ApplicationInfo handle(CreateApplicationCommand command) {
        return execute(command);
    }

    private void handleDomainEvents(Application app) {
        app.getEvents().forEach(outboxEventService::storeOutboxEvent);
        app.clearEvents();
    }
}
