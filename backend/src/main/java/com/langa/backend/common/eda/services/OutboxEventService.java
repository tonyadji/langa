package com.langa.backend.common.eda.services;

import com.langa.backend.common.eda.model.DomainEvent;

public interface OutboxEventService {

    void storeOutboxEvent(DomainEvent event);
}
