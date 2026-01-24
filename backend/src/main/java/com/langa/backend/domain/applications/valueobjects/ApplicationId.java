package com.langa.backend.domain.applications.valueobjects;

import com.langa.backend.common.utils.KeyGenerator;
import com.langa.backend.domain.applications.Application;

import java.util.UUID;

public record ApplicationId(
        String id,
        String key
) {

    public static ApplicationId newId() {
        return new ApplicationId(UUID.randomUUID().toString(), KeyGenerator.generateAppKey());
    }

    public static ApplicationId of(String id, String key) {
        return new ApplicationId(id, key);
    }
}
