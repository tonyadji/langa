package com.langa.backend.domain.applications.valueobjects;

import lombok.Data;

import java.util.List;

@Data
public class PaginatedResult<T> {
    private final List<T> content;
    private final long totalElements;
    private final int totalPages;
    private final int page;
    private final int size;

    public PaginatedResult(List<T> content, long totalElements, int totalPages, int page, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.page = page;
        this.size = size;
    }

    // getters
}
