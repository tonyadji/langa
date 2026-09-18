package com.langa.backend.domain.applications.services;

import com.langa.backend.domain.applications.valueobjects.Entry;

import java.util.List;

public interface IngestionSizeCalculator {

    long calculateSizeInBytes(List<? extends Entry> content);
}
