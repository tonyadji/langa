package com.langa.backend.domain.applications.repositories;

import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.domain.applications.valueobjects.MetricFilter;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;

import java.util.List;

public interface MetricEntryRepository {

    MetricEntry save(MetricEntry metricEntry);

    List<MetricEntry> findByAppKeyOrderByTimestampDesc(String id);

    List<MetricEntry> findByAppKeyAndAccountKeyOrderByTimestampDesc(String appKey, String accountKey);

    List<MetricEntry> saveAll(List<MetricEntry> metrics);

    PaginatedResult<MetricEntry> findFiltered(String appKey, String accountKey, MetricFilter filter, int page, int size);

}
