package com.langa.backend.domain.applications.usecases.updatepolicy;

import com.langa.backend.domain.applications.Application;

public interface IUpdateRetentionPolicyUseCase {
    Application execute(UpdateRetentionPolicyCommand command);
}
