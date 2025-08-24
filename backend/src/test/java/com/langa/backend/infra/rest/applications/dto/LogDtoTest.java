package com.langa.backend.infra.rest.applications.dto;

import com.langa.backend.domain.applications.valueobjects.LogEntry;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LogDtoTest {

    private final Validator validator;

    public LogDtoTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void toLogEntry_shouldMapCorrectly() {
        String now = LocalDateTime.now().toString();
        LogDto dto = new LogDto("Message", "INFO", "MyLogger", now);

        LogEntry entry = dto.toLogEntry();

        assertThat(entry.getMessage()).isEqualTo("Message");
        assertThat(entry.getLevel()).isEqualTo("INFO");
        assertThat(entry.getLoggerName()).isEqualTo("MyLogger");
        assertThat(entry.getTimestamp()).isEqualTo(LocalDateTime.parse(now));
    }

    @Test
    void of_shouldMapFromLogEntry() {
        LogEntry entry = new LogEntry()
                .setMessage("Hello")
                .setLevel("DEBUG")
                .setLoggerName("TestLogger")
                .setTimestamp(LocalDateTime.of(2024, 1, 1, 12, 0));

        LogDto dto = LogDto.of(entry);

        assertThat(dto.message()).isEqualTo("Hello");
        assertThat(dto.level()).isEqualTo("DEBUG");
        assertThat(dto.loggerName()).isEqualTo("TestLogger");
        assertThat(dto.timestamp()).isEqualTo("2024-01-01T12:00");
    }

    @Test
    void toLogEntry_shouldThrow_whenTimestampIsInvalid() {
        LogDto dto = new LogDto("Message", "ERROR", "MyLogger", "invalid-timestamp");

        assertThatThrownBy(dto::toLogEntry)
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void validation_shouldFail_whenFieldsAreBlankOrTooLong() {
        LogDto dto = new LogDto("", "", "", ""); // all blank

        Set<ConstraintViolation<LogDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("Log message is required",
                        "Log level is required",
                        "Logger name is required",
                        "Timestamp is required");
    }
}
