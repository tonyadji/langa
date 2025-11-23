package com.langa.backend.infra.adapters.in.rest.applications.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateApplicationRequestDto(
        @NotBlank String name) {}
