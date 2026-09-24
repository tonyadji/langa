package com.langa.backend.common.eda.publishers;

import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.events.ApplicationCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocalEventPublisherTest {

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private LocalEventPublisher eventPublisher;

    @Test
    void publish_shouldPublishSingleEvent() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        ApplicationCreatedEvent event = ApplicationCreatedEvent.of(app);

        eventPublisher.publish(event);

        verify(publisher).publishEvent(event);
    }

    @Test
    void publish_shouldPublishListOfEvents() {
        Application app = Application.createNew("My App", "ACC-1", "owner@example.com");
        ApplicationCreatedEvent event = ApplicationCreatedEvent.of(app);

        eventPublisher.publish(List.of(event));

        verify(publisher).publishEvent(event);
    }
}
