package com.langa.backend.common.eda;

import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.AbstractModel;

public class EventPublishingUseCase<T extends AbstractModel> {

    private final OutboxEventService outboxEventService;

    protected EventPublishingUseCase(OutboxEventService outboxEventService) {
        this.outboxEventService = outboxEventService;
    }

    public void handleDomainEvents(T t) {
        t.getEvents().forEach(outboxEventService::storeOutboxEvent);
        t.clearEvents();
    }
}
