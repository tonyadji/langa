package com.langa.backend.domain.applications.repositories;

import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.domain.applications.valueobjects.MetricFilter;
import com.langa.backend.domain.applications.valueobjects.PaginatedResult;

import java.util.List;

public interface MetricQueryRepository {

    List<MetricEntry> findByAppKeyOrderByTimestampDesc(String id);

    List<MetricEntry> findByAppKeyAndAccountKeyOrderByTimestampDesc(String appKey, String accountKey);

    PaginatedResult<MetricEntry> findFiltered(String appKey, String accountKey, MetricFilter filter, int page, int size);

}
