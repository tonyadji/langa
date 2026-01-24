package com.langa.backend.domain.applications.valueobjects;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MetricFilter {
    private String name;
    private String status;
    private String uri;
    private String httpMethod;
    private Integer httpStatus;
    private Integer durationLessThan;
    private Integer durationGreaterThan;
    private String keyword;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}