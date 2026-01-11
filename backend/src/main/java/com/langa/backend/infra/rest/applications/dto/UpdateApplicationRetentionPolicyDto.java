package com.langa.backend.infra.rest.applications.dto;

import com.langa.backend.domain.applications.usecases.updatepolicy.UpdateRetentionPolicyCommand;

import java.time.temporal.ChronoUnit;

public record UpdateApplicationRetentionPolicyDto(
        long duration,
        ChronoUnit unit
) {

    public UpdateRetentionPolicyCommand toCommand(String appId, String username) {
        return new UpdateRetentionPolicyCommand(appId, username, duration, unit);
    }
}
