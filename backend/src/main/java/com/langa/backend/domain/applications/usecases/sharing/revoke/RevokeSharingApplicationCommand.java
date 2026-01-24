package com.langa.backend.domain.applications.usecases.sharing.revoke;

import com.langa.backend.common.commands.Command;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.domain.applications.valueobjects.SharedWithProfile;

public record RevokeSharingApplicationCommand(
        String appId,
        String owner,
        String shareWith,
        SharedWithProfile profile
) implements Command<ApplicationInfo> {

    public RevokeSharingApplicationCommand {
        if (appId == null || appId.isBlank()) {
            throw new ApplicationException("appId is required", null, Errors.VALIDATION_ERROR);
        }

        if (owner == null || owner.isBlank()) {
            throw new ApplicationException("owner is required", null, Errors.VALIDATION_ERROR);
        }

        if (shareWith == null || shareWith.isBlank()) {
            throw new ApplicationException("shareWith is required", null, Errors.VALIDATION_ERROR);
        }

        if (profile == null) {
            throw new ApplicationException("profile is required", null, Errors.VALIDATION_ERROR);
        }
    }
}
