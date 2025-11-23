package com.langa.backend.infra.kafka.ingest.dto;

import lombok.Data;

/**
 * DTO representing Kafka message headers for ingestion
 */
@Data
public class KafkaIngestionHeaders {
    private String xAppKey;
    private String xTimestamp;
    private String xAgentSignature;
    private String xAccountKey;
    private String xUserAgent;
}