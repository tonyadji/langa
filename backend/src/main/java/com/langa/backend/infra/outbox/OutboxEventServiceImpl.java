package com.langa.backend.infra.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.model.OutboxEvent;
import com.langa.backend.common.eda.repositories.OutboxEventRepository;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.infra.outbox.exceptions.OutboxException;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventServiceImpl implements OutboxEventService {

    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;

    public OutboxEventServiceImpl(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public void storeOutboxEvent(DomainEvent event) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new OutboxException("Error processing payload", e, Errors.INTERNAL_SERVER_ERROR);
        }
        final OutboxEvent outboxEvent = OutboxEvent.createNew(
                event.getAggregateType(),
                event.getAggregateId(),
                event.getEventType().name(),
                payload
        );
        outboxEventRepository.save(outboxEvent);
    }
}
