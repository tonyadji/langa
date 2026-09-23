package com.langa.backend.infra.adapters.outbox.processors;

import com.langa.backend.common.eda.model.OutboxEvent;
import com.langa.backend.common.eda.repositories.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {
    private final OutboxEventRepository outboxEventRepository;
    private final TransactionalOutboxEventProcessor transactionalOutboxEventProcessor;

    /** Failed events are retried at each run until this number of attempts, then abandoned. */
    static final int MAX_ATTEMPTS = 5;

    @Scheduled(fixedDelay = 5000)
    public void process() {
        log.trace("Processing Outbox Events");
        List<OutboxEvent> events = outboxEventRepository.findPending();
        for (OutboxEvent outboxEvent : events) {
            try {
                transactionalOutboxEventProcessor.processSingleEvent(outboxEvent);
            } catch (Exception ex) {
                recordFailure(outboxEvent, ex);
            }
        }
    }

    private void recordFailure(OutboxEvent outboxEvent, Exception ex) {
        // An undeserializable event is already abandoned by the transactional processor
        final boolean abandoned = outboxEvent.isError() || outboxEvent.recordFailure(MAX_ATTEMPTS);
        outboxEventRepository.save(outboxEvent);
        if (abandoned) {
            log.error("Outbox event {} ({}) abandoned after {} attempt(s)",
                    outboxEvent.getId(), outboxEvent.getEventType(), Math.max(1, outboxEvent.getAttempts()), ex);
        } else {
            log.warn("Failed to process outbox event {} ({}), attempt {}/{}: {}",
                    outboxEvent.getId(), outboxEvent.getEventType(), outboxEvent.getAttempts(), MAX_ATTEMPTS, ex.getMessage());
        }
    }
}
