package com.langa.backend.domain.applications.usecases.updatepolicy;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;

@UseCase
public class UpdateRetentionPolicyUseCase implements IUpdateRetentionPolicyUseCase, CommandHandler<UpdateRetentionPolicyCommand, Application> {

    private final ApplicationRepository applicationRepository;

    public UpdateRetentionPolicyUseCase(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public Application handle(UpdateRetentionPolicyCommand command) {
        return execute(command);
    }

    @Override
    public Application execute(UpdateRetentionPolicyCommand command) {
        Application application = applicationRepository.securedFindByIdAndOwner(command.appId(), command.owner())
                .orElseThrow(() -> new ApplicationException("Application not found", null, Errors.APPLICATION_NOT_FOUND));

        application.checkOwnership(command.owner());

        application.updateRetentionPolicy(command.toRetentionPolicy());
        applicationRepository.save(application);
        return application;
    }
}
