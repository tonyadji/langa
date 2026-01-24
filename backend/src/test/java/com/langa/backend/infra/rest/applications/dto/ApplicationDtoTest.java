package com.langa.backend.infra.rest.applications.dto;

import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationDtoTest {

    @Test
    void of_shouldMapFromApplicationInfo() {
        ApplicationInfo info = new ApplicationInfo("app-id", "MyApp", "APP-213", "U-123","owner@example.com", null);

        ApplicationDto dto = ApplicationDto.of(info);

        assertThat(dto.id()).isEqualTo("app-id");
        assertThat(dto.name()).isEqualTo("MyApp");
        assertThat(dto.accountKey()).isEqualTo("U-123");
        assertThat(dto.owner()).isEqualTo("owner@example.com");
    }

    @Test
    void recordAccessors_shouldWorkCorrectly() {
        ApplicationDto dto = new ApplicationDto("id-001", "TestApp", "acc-999", "me@example.com", null);

        assertThat(dto.id()).isEqualTo("id-001");
        assertThat(dto.name()).isEqualTo("TestApp");
        assertThat(dto.accountKey()).isEqualTo("acc-999");
        assertThat(dto.owner()).isEqualTo("me@example.com");
    }
}
