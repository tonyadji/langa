package com.langa.backend.infra.rest.applications.dto;

import com.langa.backend.domain.applications.usecases.create.CreateApplicationCommand;
import jakarta.validation.constraints.NotBlank;

public record CreateApplicationRequestDto(
        @NotBlank String name) {

    public CreateApplicationCommand toCommand(String owner) {
        return new CreateApplicationCommand(name, owner);
    }
}
