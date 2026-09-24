package com.langa.backend.domain.applications.usecases.create;

import com.langa.backend.common.commands.Command;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;

public record CreateApplicationCommand(String name, String ownerEmail)
 implements Command<ApplicationInfo> {

    public CreateApplicationCommand {
        if (name == null || name.isEmpty()) {
            throw new ApplicationException("name cannot be null or empty", null, Errors.VALIDATION_ERROR);
        }

        if (ownerEmail == null || ownerEmail.isEmpty()) {
            throw new ApplicationException("ownerEmail cannot be null or empty", null, Errors.VALIDATION_ERROR);
        }
    }
}
