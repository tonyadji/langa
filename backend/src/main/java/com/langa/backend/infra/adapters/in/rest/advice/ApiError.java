package com.langa.backend.infra.adapters.in.rest.advice;

import com.langa.backend.common.model.errors.GenericException;
import lombok.Data;

import java.util.Objects;

@Data
public class ApiError {
    private String code;
    private String message;
    private String details;

    public ApiError(String code, String message, String details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public static ApiError of(GenericException gex) {
        return new ApiError(gex.getError().getCode(),
                gex.getError().getMessage(),
                Objects.isNull(gex.getCause()) ?
                        gex.getMessage() :
                gex.getCause().toString());
    }

    public static ApiError of(String message, String code, String details) {
        return new ApiError(code,
                message,
                details);
    }
}
