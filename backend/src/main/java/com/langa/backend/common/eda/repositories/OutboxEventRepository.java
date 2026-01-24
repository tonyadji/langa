package com.langa.backend.common.eda.repositories;

import com.langa.backend.common.eda.model.OutboxEvent;

import java.util.List;

public interface OutboxEventRepository {

    void save(OutboxEvent event);

    List<OutboxEvent> findAllByProcessedFalse();
}
