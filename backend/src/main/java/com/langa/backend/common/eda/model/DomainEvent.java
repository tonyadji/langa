package com.langa.backend.common.eda.model;

import com.langa.backend.common.eda.registry.EventTypeRegistry;

public interface DomainEvent {

    EventTypeRegistry getEventType();
    String getAggregateType();
    String getAggregateId();
}
