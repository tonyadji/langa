package com.langa.backend.domain.applications.usecases.updatepolicy;

import com.langa.backend.common.commands.Command;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.valueobjects.RetentionPolicy;

import java.time.temporal.ChronoUnit;

public record UpdateRetentionPolicyCommand(
        String appId,
        String owner,
        long duration,
        ChronoUnit unit
) implements Command<Application> {

    public UpdateRetentionPolicyCommand {
        if (appId == null || appId.isBlank()) {
            throw new ApplicationException("appId cannot be null or empty", null, Errors.VALIDATION_ERROR);
        }
        if (owner == null || owner.isBlank()) {
            throw new ApplicationException("owner cannot be null or empty", null, Errors.VALIDATION_ERROR);
        }
        if (duration <= 0) {
            throw new ApplicationException("duration must be greater than 0", null, Errors.VALIDATION_ERROR);
        }
        if (unit == null) {
            throw new ApplicationException("unit cannot be null", null, Errors.VALIDATION_ERROR);
        }
    }

    public RetentionPolicy toRetentionPolicy() {
        return new RetentionPolicy(duration, unit, null);
    }
}
