package com.langa.backend.infra.services.applications;

import com.langa.backend.domain.applications.services.IngestionSizeCalculator;
import com.langa.backend.domain.applications.valueobjects.Entry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngestionSizeCalculatorImpl implements IngestionSizeCalculator {
    @Override
    public long calculateSizeInBytes(List<? extends Entry> content) {
        return content.stream().mapToLong(Entry::getSizeInBytes).sum();
    }
}
