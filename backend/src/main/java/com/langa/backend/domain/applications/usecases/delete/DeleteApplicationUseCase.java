package com.langa.backend.domain.applications.usecases.delete;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;

@UseCase
public class DeleteApplicationUseCase implements IDeleteApplicationUseCase, CommandHandler<DeleteApplicationCommand, String> {

    private final ApplicationRepository applicationRepository;

    public DeleteApplicationUseCase(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public String handle(DeleteApplicationCommand command) {
        execute(command);
        return "App Deleted Successfully";
    }

    @Override
    public void execute(DeleteApplicationCommand command) {
        Application application = applicationRepository.securedFindByIdAndOwner(command.appId(), command.owner())
                .orElseThrow(() -> new ApplicationException("Application not found", null, Errors.APPLICATION_NOT_FOUND));

        application.checkOwnership(command.owner());

        applicationRepository.deleteById(command.appId());
    }
}
