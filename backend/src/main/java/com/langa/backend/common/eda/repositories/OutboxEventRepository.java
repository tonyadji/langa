package com.langa.backend.common.eda.repositories;

import com.langa.backend.common.eda.model.OutboxEvent;

import java.util.List;

public interface OutboxEventRepository {

    void save(OutboxEvent event);

    /** Events neither processed nor abandoned after too many failures. */
    List<OutboxEvent> findPending();
}
