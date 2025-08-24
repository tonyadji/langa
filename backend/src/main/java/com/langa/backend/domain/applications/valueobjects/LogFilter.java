package com.langa.backend.domain.applications.valueobjects;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
public class LogFilter {
    private String logLevel;
    private String keyword;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public LogFilter(String logLevel, String keyword, LocalDateTime startDate, LocalDateTime endDate) {
        this.logLevel = logLevel;
        this.keyword = keyword;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}