package com.langa.backend.domain.applications.usecases.sharing.revoke;

import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;

public interface IRevokeSharingUseCase {

    ApplicationInfo execute(RevokeSharingApplicationCommand command);
}
