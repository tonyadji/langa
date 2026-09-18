package com.langa.backend.domain.applications.repositories;

import java.time.LocalDateTime;

public interface ApplicationNonceRepository {

    boolean existsByAppKeyAndNonce(String appKey, String nonce);
    void save(String appKey, String nonce, LocalDateTime usageDate);
}
