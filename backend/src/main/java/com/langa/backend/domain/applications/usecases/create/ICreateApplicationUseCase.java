package com.langa.backend.domain.applications.usecases.create;

import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;

public interface ICreateApplicationUseCase {

    ApplicationInfo execute(CreateApplicationCommand command);
}
