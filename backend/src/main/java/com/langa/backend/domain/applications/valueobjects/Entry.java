package com.langa.backend.domain.applications.valueobjects;

import java.nio.charset.StandardCharsets;

public interface Entry {

    int BASE_DOCUMENT_OVERHEAD = 100;
    int TIMESTAMP_SIZE = 10;
    int FIELD_SIZE_OVERHEAD = 5;
    long getSizeInBytes();

    default long getStringSize(String value) {
        if (value == null) {
            return 1;
        }
        return (value.getBytes(StandardCharsets.UTF_8).length + FIELD_SIZE_OVERHEAD);
    }
}
