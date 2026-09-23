package com.langa.backend.infra.rest.advice;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.common.model.errors.GenericException;
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
    void handleAll_shouldReturnInternalServerError_withoutLeakingExceptionMessage() {
        Exception ex = new RuntimeException("Something went wrong - internal details");

        ResponseEntity<ApiError> response = controllerAdvice.handleAll(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(Errors.INTERNAL_SERVER_ERROR.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo(Errors.INTERNAL_SERVER_ERROR.getMessage());
        assertThat(response.getBody().getDetails()).isNull();
    }

    @Test
    void unreadableBody_shouldAnswerWithTheNestedBusinessError() {
        GenericException tooLarge = new GenericException("too large", null, Errors.INGESTION_PAYLOAD_TOO_LARGE);
        org.springframework.http.converter.HttpMessageNotReadableException ex =
                new org.springframework.http.converter.HttpMessageNotReadableException("I/O error", tooLarge,
                        mock(org.springframework.http.HttpInputMessage.class));

        ResponseEntity<ApiError> response = controllerAdvice.handleUnreadableBody(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(413);
        assertThat(response.getBody().getCode()).isEqualTo("413-000");
    }

    @Test
    void unreadableBody_shouldAnswer400ForMalformedJson() {
        org.springframework.http.converter.HttpMessageNotReadableException ex =
                new org.springframework.http.converter.HttpMessageNotReadableException("JSON parse error",
                        new IllegalArgumentException("Unexpected character"),
                        mock(org.springframework.http.HttpInputMessage.class));

        ResponseEntity<ApiError> response = controllerAdvice.handleUnreadableBody(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getDetails()).isEqualTo("Malformed request body");
    }

    @Test
    void apiError_shouldAnswerServerErrorsWithTheirStatus() {
        GenericException gex = new GenericException("boom", null, Errors.INTERNAL_SERVER_ERROR);

        assertThat(controllerAdvice.apiError(gex).getStatusCode().value()).isEqualTo(500);
    }
}
