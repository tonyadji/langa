package com.langa.backend.domain.applications.repositories;

import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.LogFilter;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;

import java.time.LocalDateTime;
import java.util.List;

public interface LogEntryRepository {

    LogEntry save(LogEntry logEntry);

    List<LogEntry> findByAppKeyOrderByTimestampDesc(String id);

    List<LogEntry> findByAppKeyAndAccountKeyOrderByTimestampDesc(String appKey, String accountKey);

    List<LogEntry> saveAll(List<LogEntry> logs);

    PaginatedResult<LogEntry> findFiltered(String appKey, String accountKey, LogFilter filter, int page, int size);

}
