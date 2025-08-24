package com.langa.backend.domain.applications.valueobjects;

import com.langa.backend.domain.applications.Application;

public record ApplicationInfo(String id, String name, String key, String accountKey, String owner) {

    public static ApplicationInfo of(Application application) {
        return new ApplicationInfo(application.getId(), application.getName(), application.getKey(), application.getAccountKey(), application.getOwner());
    }
}
