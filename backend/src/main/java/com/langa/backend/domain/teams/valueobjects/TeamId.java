package com.langa.backend.domain.teams.valueobjects;

import com.langa.backend.common.utils.KeyGenerator;

import java.util.UUID;

public record TeamId(
        String id,
        String key
) {
    public static TeamId of(String name, String createdBy) {
        return new TeamId(UUID.nameUUIDFromBytes(name.concat(createdBy).getBytes()).toString(), KeyGenerator.generateTeamKey(name, createdBy));
    }
}
