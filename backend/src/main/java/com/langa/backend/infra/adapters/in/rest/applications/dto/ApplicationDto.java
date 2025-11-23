package com.langa.backend.infra.adapters.in.rest.applications.dto;

import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.domain.applications.valueobjects.ShareWith;

import java.util.Set;

public record ApplicationDto(String id, String name, String accountKey, String owner, Set<ShareWith> shareWith) {
    public static ApplicationDto of(ApplicationInfo applicationInfo) {
        return new ApplicationDto(
                applicationInfo.id(),
                applicationInfo.name(),
                applicationInfo.accountKey(),
                applicationInfo.owner(),
                applicationInfo.sharedWith()
        );
    }
}
