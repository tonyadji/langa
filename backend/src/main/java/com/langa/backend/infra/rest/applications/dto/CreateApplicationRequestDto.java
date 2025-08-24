package com.langa.backend.infra.rest.applications.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateApplicationRequestDto(
        @NotBlank String name) {}
