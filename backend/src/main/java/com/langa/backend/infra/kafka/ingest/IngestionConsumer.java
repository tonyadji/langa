package com.langa.backend.infra.kafka.ingest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.services.IngestionCredentials;
import com.langa.backend.domain.applications.valueobjects.IngestionType;
import com.langa.backend.infra.kafka.exceptions.KafkaIngestionException;
import com.langa.backend.infra.kafka.ingest.services.KafkaCredentialsMapper;
import com.langa.backend.infra.rest.common.dto.LogDto;
import com.langa.backend.infra.rest.common.dto.MetricDto;
import com.langa.backend.infra.rest.ingest.dto.IngestionRequestDto;
import com.langa.backend.infra.rest.ingest.dto.LogIngestionRequestDto;
import com.langa.backend.infra.rest.ingest.dto.MetricIngestionRequestDto;
import com.langa.backend.infra.adapters.services.applications.IngestionService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for processing ingestion messages from the 'langa' topic
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IngestionConsumer {

    private final IngestionService ingestionService;
    private final KafkaCredentialsMapper kafkaCredentialsMapper;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "langa", groupId = "langa-ingestion-group")
    public void consumeIngestionMessage(ConsumerRecord<String, String> messageRecord) {
        try {
            log.info("Received message from topic 'langa': partition={}, offset={}, key={}", 
                    messageRecord.partition(), messageRecord.offset(), messageRecord.key());
            log.debug("Message payload: {}", messageRecord.value());

            IngestionCredentials credentials = kafkaCredentialsMapper.mapFromKafkaHeaders(messageRecord);
            log.debug("Extracted credentials from headers: appKey={}, accountKey={}, timestamp={}", 
                    credentials.appKey(), credentials.accountKey(), credentials.timestamp());

            Map<String, Object> ingestionRequestMap = objectMapper.readValue(messageRecord.value(), Map.class);

            IngestionRequestDto ingestionRequest = mapToIngestionRequestDto(ingestionRequestMap);

            log.trace("Parsed ingestion request: type={}, appKey={}, accountKey={}, entries count={}",
                    ingestionRequest.getClass().getSimpleName(), 
                    getAppKey(ingestionRequest), 
                    getAccountKey(ingestionRequest), 
                    getEntriesCount(ingestionRequest));

            ingestionService.process(ingestionRequest, credentials);
            
            log.debug("Successfully processed ingestion message from Kafka: type={}, appKey={}",
                    ingestionRequest.getClass().getSimpleName(), getAppKey(ingestionRequest));

        } catch (Exception e) {
            log.error("Error processing Kafka ingestion message: topic={}, partition={}, offset={}, error={}", 
                    messageRecord.topic(), messageRecord.partition(), messageRecord.offset(), e.getMessage(), e);
            // handle retry or dead letter queue logic
            throw new KafkaIngestionException("Failed to process Kafka ingestion message", e, Errors.INTERNAL_SERVER_ERROR);
        }
    }

  private IngestionRequestDto mapToIngestionRequestDto(Map<String, Object> ingestionRequestMap) {
    IngestionType type = IngestionType.valueOf((String) ingestionRequestMap.get("type"));
    String appKey = (String) ingestionRequestMap.get("appKey");
    String accountKey = (String) ingestionRequestMap.get("accountKey");

    return switch (type) {
      case LOG -> {
        List<LogDto> logEntries = ((List<Map<String, Object>>) ingestionRequestMap.get("entries"))
            .stream()
            .map(this::mapToLogDto)
            .toList();
        yield new LogIngestionRequestDto(appKey, accountKey, logEntries, type);
      }
      case METRIC -> {
        List<MetricDto> metricEntries = ((List<Map<String, Object>>) ingestionRequestMap.get("entries"))
            .stream()
            .map(this::mapToMetricDto)
            .toList();
        yield new MetricIngestionRequestDto(appKey, accountKey, metricEntries, type);
      }
    };
  }

  private LogDto mapToLogDto(Map<String, Object> logMap) {
    return new LogDto(
        (String) logMap.get("message"),
        (String) logMap.get("level"),
        (String) logMap.get("loggerName"),
        (String) logMap.get("timestamp")
    );
  }

  private MetricDto mapToMetricDto(Map<String, Object> metricMap) {
    return new MetricDto(
        (String) metricMap.get("name"),
        (Integer) metricMap.get("durationMillis"),
        (String) metricMap.get("status"),
        String.valueOf(metricMap.get("timestamp")),
        (String) metricMap.get("uri"),
        (String) metricMap.get("httpMethod"),
        (Integer) metricMap.get("httpStatus")
    );
  }

  private String getAppKey(IngestionRequestDto request) {
        if (request instanceof LogIngestionRequestDto logRequest) {
            return logRequest.appKey();
        } else if (request instanceof MetricIngestionRequestDto metricRequest) {
            return metricRequest.appKey();
        }
        return "unknown";
    }

    private String getAccountKey(IngestionRequestDto request) {
        if (request instanceof LogIngestionRequestDto logRequest) {
            return logRequest.accountKey();
        } else if (request instanceof MetricIngestionRequestDto metricRequest) {
            return metricRequest.accountKey();
        }
        return "unknown";
    }

    private int getEntriesCount(IngestionRequestDto request) {
        if (request instanceof LogIngestionRequestDto logRequest) {
            return logRequest.entries() != null ? logRequest.entries().size() : 0;
        } else if (request instanceof MetricIngestionRequestDto metricRequest) {
            return metricRequest.entries() != null ? metricRequest.entries().size() : 0;
        }
        return 0;
    }
}
