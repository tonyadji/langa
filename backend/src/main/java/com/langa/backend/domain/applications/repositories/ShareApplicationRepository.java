package com.langa.backend.domain.applications.repositories;

import com.langa.backend.domain.applications.valueobjects.ShareWith;

public interface ShareApplicationRepository {
    ShareWith save(ShareWith shareWith);
}
