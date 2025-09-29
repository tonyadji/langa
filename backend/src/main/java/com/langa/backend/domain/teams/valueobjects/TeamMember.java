package com.langa.backend.domain.teams.valueobjects;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TeamMember(
        @NotNull String email,
        @NotNull TeamRole role,
        @NotNull String teamKey,
        LocalDateTime addedDate
        ) {
}
