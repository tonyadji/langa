package com.langa.backend.domain.applications.repositories;

import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.LogFilter;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;

import java.util.List;

public interface LogQueryRepository {

    List<LogEntry> findByAppKeyOrderByTimestampDesc(String id);

    List<LogEntry> findByAppKeyAndAccountKeyOrderByTimestampDesc(String appKey, String accountKey);

    PaginatedResult<LogEntry> findFiltered(String appKey, String accountKey, LogFilter filter, int page, int size);
}
