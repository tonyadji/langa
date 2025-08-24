package com.langa.backend.infra.rest.applications.dto;

import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;

public record ApplicationDto(String id, String name, String accountKey, String owner) {
    public static ApplicationDto of(ApplicationInfo applicationInfo) {
        return new ApplicationDto(applicationInfo.id(), applicationInfo.name(), applicationInfo.accountKey(), applicationInfo.owner());
    }
}
