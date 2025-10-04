package com.langa.backend.infra.outbox.processors;

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

    @Scheduled(fixedDelay = 5000)
    public void process() {
        log.info("Processing Outbox Events");
        List<OutboxEvent> events = outboxEventRepository.findAllByProcessedFalse();
        for (OutboxEvent outboxEvent : events) {
            try {
                transactionalOutboxEventProcessor.processSingleEvent(outboxEvent);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }

        }
    }
}
