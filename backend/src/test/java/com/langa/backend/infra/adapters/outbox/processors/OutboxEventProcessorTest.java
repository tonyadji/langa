package com.langa.backend.infra.adapters.outbox.processors;

import com.langa.backend.common.eda.model.OutboxEvent;
import com.langa.backend.common.eda.repositories.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventProcessorTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;
    @Mock
    private TransactionalOutboxEventProcessor transactionalOutboxEventProcessor;

    @InjectMocks
    private OutboxEventProcessor processor;

    private static OutboxEvent event(String id) {
        return OutboxEvent.createNew("Team", "team-1", "TEAM_INVITATION_EMAIL", "{}").setId(id);
    }

    @Test
    void process_shouldProcessEveryPendingEvent() {
        OutboxEvent first = event("e1");
        OutboxEvent second = event("e2");
        when(outboxEventRepository.findPending()).thenReturn(List.of(first, second));

        processor.process();

        verify(transactionalOutboxEventProcessor).processSingleEvent(first);
        verify(transactionalOutboxEventProcessor).processSingleEvent(second);
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void process_shouldRecordAFailedAttemptAndKeepProcessingTheOthers() {
        OutboxEvent failing = event("e1");
        OutboxEvent next = event("e2");
        when(outboxEventRepository.findPending()).thenReturn(List.of(failing, next));
        doThrow(new IllegalStateException("boom")).when(transactionalOutboxEventProcessor).processSingleEvent(failing);

        processor.process();

        assertEquals(1, failing.getAttempts());
        assertFalse(failing.isError(), "still retried at the next run");
        verify(outboxEventRepository).save(failing);
        verify(transactionalOutboxEventProcessor).processSingleEvent(next);
    }

    @Test
    void process_shouldAbandonAnEventAfterTheMaximumAttempts() {
        OutboxEvent failing = event("e1").setAttempts(OutboxEventProcessor.MAX_ATTEMPTS - 1);
        when(outboxEventRepository.findPending()).thenReturn(List.of(failing));
        doThrow(new IllegalStateException("boom")).when(transactionalOutboxEventProcessor).processSingleEvent(failing);

        processor.process();

        assertEquals(OutboxEventProcessor.MAX_ATTEMPTS, failing.getAttempts());
        assertTrue(failing.isError(), "no longer selected as pending");
        verify(outboxEventRepository).save(failing);
    }

    @Test
    void process_shouldKeepAnEventAlreadyAbandonedByTheTransactionalProcessor() {
        OutboxEvent undeserializable = event("e1");
        when(outboxEventRepository.findPending()).thenReturn(List.of(undeserializable));
        doAnswer(invocation -> {
            undeserializable.setError(true);
            throw new IllegalStateException("Deserialization failed");
        }).when(transactionalOutboxEventProcessor).processSingleEvent(undeserializable);

        processor.process();

        assertTrue(undeserializable.isError());
        assertEquals(0, undeserializable.getAttempts());
        verify(outboxEventRepository).save(undeserializable);
    }

    @Test
    void recordFailure_shouldAbandonOnlyAtTheMaximum() {
        OutboxEvent outboxEvent = event("e1");

        assertFalse(outboxEvent.recordFailure(2));
        assertTrue(outboxEvent.recordFailure(2));
        assertEquals(2, outboxEvent.getAttempts());
    }
}
