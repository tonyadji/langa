package com.langa.backend.common.eda.publishers;

import com.langa.backend.common.eda.model.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LocalEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher publisher;

    public LocalEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(DomainEvent event) {
        log.info("Publishing event: {}", event);
        publisher.publishEvent(event);
    }

    @Override
    public void publish(List<DomainEvent> events) {
        events.forEach(event -> {
            log.info("Publishing event: {}", event);
            publisher.publishEvent(event);
        });
    }
}
