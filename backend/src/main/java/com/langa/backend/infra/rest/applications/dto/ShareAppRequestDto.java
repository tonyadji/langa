package com.langa.backend.infra.rest.applications.dto;

import com.langa.backend.domain.applications.valueobjects.SharedWithProfile;

public record ShareAppRequestDto(
        String sharedWith,
        SharedWithProfile profile
) {

}
