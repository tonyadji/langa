package com.langa.backend.common.eda.publishers;

import com.langa.backend.common.eda.model.DomainEvent;

import java.util.List;

public interface DomainEventPublisher {

    void publish(DomainEvent domainEvent);
    void publish(List<DomainEvent> events);
}
