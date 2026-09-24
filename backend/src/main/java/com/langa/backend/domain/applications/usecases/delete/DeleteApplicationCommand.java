package com.langa.backend.domain.applications.usecases.delete;

import com.langa.backend.common.commands.Command;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.exceptions.ApplicationException;

public record DeleteApplicationCommand(
        String appId,
        String owner
) implements Command<String> {

    public DeleteApplicationCommand {
        if (appId == null || appId.isBlank()) {
            throw new ApplicationException("Application ID cannot be null or blank", null, Errors.VALIDATION_ERROR);
        }
        if (owner == null || owner.isBlank()) {
            throw new ApplicationException("Owner cannot be null or blank", null, Errors.VALIDATION_ERROR);
        }
    }
}
