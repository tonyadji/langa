package com.langa.backend.infra.rest.advice;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;
import com.langa.backend.infra.adapters.in.rest.advice.ApiError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorTest {

    @Test
    void constructor_shouldSetFields() {
        ApiError error = new ApiError("404", "Not Found", "Resource missing");

        assertThat(error.getCode()).isEqualTo("404");
        assertThat(error.getMessage()).isEqualTo("Not Found");
        assertThat(error.getDetails()).isEqualTo("Resource missing");
    }

    @Test
    void ofGenericException_withoutCause_shouldUseMessageAsDetails() {
        Errors error = Errors.VALIDATION_ERROR;
        GenericException gex = new GenericException("Some validation failed", null, error);

        ApiError apiError = ApiError.of(gex);

        assertThat(apiError.getCode()).isEqualTo("400");
        assertThat(apiError.getMessage()).isEqualTo("Validation error");
        assertThat(apiError.getDetails()).isEqualTo("Some validation failed");
    }

    @Test
    void ofStringMessage_shouldReturnApiError() {
        ApiError apiError = ApiError.of("Bad request", "400", "Missing field");

        assertThat(apiError.getCode()).isEqualTo("400");
        assertThat(apiError.getMessage()).isEqualTo("Bad request");
        assertThat(apiError.getDetails()).isEqualTo("Missing field");
    }
}