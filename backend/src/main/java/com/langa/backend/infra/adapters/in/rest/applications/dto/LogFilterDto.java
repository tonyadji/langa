package com.langa.backend.infra.adapters.in.rest.applications.dto;
import com.langa.backend.domain.applications.valueobjects.LogFilter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

public record LogFilterDto(
        String level,
        String keyword,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endDate
) {
        public LogFilter toLogFilter() {
            return new LogFilter(level, keyword, startDate, endDate);
        }
}