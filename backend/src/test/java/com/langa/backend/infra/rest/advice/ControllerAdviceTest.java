package com.langa.backend.infra.rest.advice;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;
import com.langa.backend.infra.adapters.in.rest.advice.ApiError;
import com.langa.backend.infra.adapters.in.rest.advice.ControllerAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ControllerAdviceTest {

    private ControllerAdvice controllerAdvice;

    @BeforeEach
    void setUp() {
        controllerAdvice = new ControllerAdvice();
    }

    @Test
    void apiError_shouldReturnResponseEntityWithApiError() {
        Errors error = Errors.VALIDATION_ERROR;
        GenericException gex = new GenericException("Validation failed", null, error);

        ResponseEntity<ApiError> response = controllerAdvice.apiError(gex);

        assertThat(response.getStatusCodeValue()).isEqualTo(error.getHttpCode());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(error.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo(error.getMessage());
        assertThat(response.getBody().getDetails()).isEqualTo("Validation failed");
    }

    @Test
    void handleValidation_shouldReturnBadRequestWithDetails() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "must not be null");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiError> response = controllerAdvice.handleValidation(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(Errors.VALIDATION_ERROR.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo(Errors.VALIDATION_ERROR.getMessage());
        assertThat(response.getBody().getDetails()).contains("fieldName: must not be null");
    }

    @Test
    void handleAll_shouldReturnInternalServerError() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<ApiError> response = controllerAdvice.handleAll(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(Errors.INTERNAL_SERVER_ERROR.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo(Errors.INTERNAL_SERVER_ERROR.getMessage());
        assertThat(response.getBody().getDetails()).isEqualTo("Something went wrong");
    }
}
