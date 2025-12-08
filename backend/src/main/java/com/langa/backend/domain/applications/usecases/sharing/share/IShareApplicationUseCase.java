package com.langa.backend.domain.applications.usecases.sharing.share;

import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;

public interface IShareApplicationUseCase {
    ApplicationInfo execute(ShareApplicationCommand command);
}
