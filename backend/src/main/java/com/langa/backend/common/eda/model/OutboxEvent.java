package com.langa.backend.common.eda.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class OutboxEvent {
    private String id;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String payload;
    private LocalDateTime createdDate;
    private LocalDateTime processedDate;
    private boolean processed;
    private boolean error;
    /** Failed processing attempts; the event is abandoned (error) after the maximum. */
    private int attempts;

    public static OutboxEvent createNew(String aggregateType, String aggregateId, String eventType, String payload) {
        return new OutboxEvent()
                .setAggregateType(aggregateType)
                .setAggregateId(aggregateId)
                .setEventType(eventType)
                .setPayload(payload)
                .setProcessed(false)
                .setError(false)
                .setCreatedDate(LocalDateTime.now());
    }

    /**
     * Records a failed processing attempt.
     *
     * @return true when the event is abandoned (no more retries)
     */
    public boolean recordFailure(int maxAttempts) {
        attempts++;
        if (attempts >= maxAttempts) {
            error = true;
        }
        return error;
    }
}
