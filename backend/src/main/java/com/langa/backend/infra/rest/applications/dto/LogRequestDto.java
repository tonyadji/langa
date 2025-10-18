package com.langa.backend.infra.rest.applications.dto;

import com.langa.backend.infra.rest.common.dto.LogDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record LogRequestDto(
        @NotBlank(message = "appKey is required") String appKey,
        @NotBlank(message = "accountKey is required") String accountKey,
        @NotEmpty List<LogDto> logs) {
}
