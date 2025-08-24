package com.langa.backend.infra.rest.applications.dto;

import lombok.Data;

import java.util.List;

@Data
public class PaginatedResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    public PaginatedResponse(List<T> content, long totalElements, int totalPages, int page, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.page = page;
        this.size = size;
    }
}
