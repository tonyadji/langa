package com.langa.backend.infra.outbox.processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langa.backend.common.eda.model.DomainEvent;
import com.langa.backend.common.eda.model.OutboxEvent;
import com.langa.backend.common.eda.publishers.DomainEventPublisher;
import com.langa.backend.common.eda.registry.EventTypeRegistry;
import com.langa.backend.common.eda.repositories.OutboxEventRepository;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.infra.outbox.exceptions.OutboxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionalOutboxEventProcessor {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public void processSingleEvent(OutboxEvent outboxEvent) {
        log.debug("Processing Outbox Event : {}", outboxEvent);
        try {
            Class<?> eventClass = EventTypeRegistry.valueOf(outboxEvent.getEventType()).getEventClass();
            Object eventObject = objectMapper.readValue(outboxEvent.getPayload(), eventClass);
            if (!(eventObject instanceof DomainEvent domainEvent)) {
                throw new OutboxException("The payload is not a DomainEvent", null, Errors.INTERNAL_SERVER_ERROR);
            }

            domainEventPublisher.publish(domainEvent);

            outboxEvent.setProcessed(true);
            outboxEventRepository.save(outboxEvent);
            log.debug("Outbox Event processed successfully");
        } catch (JsonProcessingException ex) {
            outboxEvent.setError(true);
            outboxEventRepository.save(outboxEvent);
            throw new OutboxException("Deserialization failed for event " + outboxEvent.getId(), ex, Errors.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            throw new OutboxException("Error occured processing outbox event", ex, Errors.INTERNAL_SERVER_ERROR);
        }
    }
}
