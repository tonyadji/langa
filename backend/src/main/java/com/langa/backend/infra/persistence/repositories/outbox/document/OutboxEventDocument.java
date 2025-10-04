package com.langa.backend.infra.persistence.repositories.outbox.document;

import com.langa.backend.common.eda.model.OutboxEvent;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document( collection = "c_outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEventDocument {
    @Id
    private String id;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String payload;
    private LocalDateTime createdDate;
    private boolean processed;
    private boolean errored;

    public OutboxEvent toOutboxEvent() {
        return new OutboxEvent()
                .setId(id)
                .setAggregateId(aggregateId)
                .setAggregateType(aggregateType)
                .setEventType(eventType)
                .setPayload(payload)
                .setProcessed(processed);
    }

    public static OutboxEventDocument of(OutboxEvent outboxEvent) {
        return OutboxEventDocument.builder()
                .id(outboxEvent.getId())
                .aggregateType(outboxEvent.getAggregateType())
                .aggregateId(outboxEvent.getAggregateId())
                .eventType(outboxEvent.getEventType())
                .payload(outboxEvent.getPayload())
                .createdDate(outboxEvent.getCreatedDate())
                .processed(outboxEvent.isProcessed())
                .errored(outboxEvent.isError())
                .build();
    }
}
