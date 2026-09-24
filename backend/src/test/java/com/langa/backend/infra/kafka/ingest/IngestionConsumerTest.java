package com.langa.backend.infra.kafka.ingest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langa.backend.domain.applications.services.IngestionCredentials;
import com.langa.backend.domain.applications.services.IngestionSecurity;
import com.langa.backend.infra.adapters.services.applications.IngestionService;
import com.langa.backend.infra.kafka.exceptions.KafkaIngestionException;
import com.langa.backend.infra.kafka.ingest.services.KafkaCredentialsMapper;
import com.langa.backend.infra.rest.ingest.dto.IngestionRequestDto;
import com.langa.backend.infra.rest.ingest.dto.LogIngestionRequestDto;
import com.langa.backend.infra.rest.ingest.dto.MetricIngestionRequestDto;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestionConsumerTest {

    @Mock
    private IngestionService ingestionService;
    @Mock
    private KafkaCredentialsMapper kafkaCredentialsMapper;

    private IngestionConsumer consumer;
    private final IngestionCredentials credentials = new IngestionCredentials(
            "agent", "app-key", "acc-key", "123", "nonce:sig", IngestionSecurity.CredentialType.KAFKA);

    @BeforeEach
    void setUp() {
        consumer = new IngestionConsumer(ingestionService, kafkaCredentialsMapper, new ObjectMapper());
    }

    @Test
    void consumeIngestionMessage_shouldProcessLogIngestion() {
        String json = """
                {"type":"LOG","appKey":"app-key","accountKey":"acc-key","entries":[
                    {"message":"hello","level":"INFO","loggerName":"logger","timestamp":"2025-01-01T00:00:00Z"}
                ]}
                """;
        ConsumerRecord<String, String> record = new ConsumerRecord<>("langa", 0, 0L, "key", json);
        when(kafkaCredentialsMapper.mapFromKafkaHeaders(record)).thenReturn(credentials);

        consumer.consumeIngestionMessage(record);

        ArgumentCaptor<IngestionRequestDto> captor = ArgumentCaptor.forClass(IngestionRequestDto.class);
        verify(ingestionService).process(captor.capture(), org.mockito.ArgumentMatchers.eq(credentials));
        assertInstanceOf(LogIngestionRequestDto.class, captor.getValue());
        assertEquals("app-key", ((LogIngestionRequestDto) captor.getValue()).appKey());
    }

    @Test
    void consumeIngestionMessage_shouldProcessMetricIngestion() {
        String json = """
                {"type":"METRIC","appKey":"app-key","accountKey":"acc-key","entries":[
                    {"name":"http.request","status":"SUCCESS","timestamp":"2025-01-01T00:00:00Z","durationMillis":100,"httpStatus":200}
                ]}
                """;
        ConsumerRecord<String, String> record = new ConsumerRecord<>("langa", 0, 0L, "key", json);
        when(kafkaCredentialsMapper.mapFromKafkaHeaders(record)).thenReturn(credentials);

        consumer.consumeIngestionMessage(record);

        ArgumentCaptor<IngestionRequestDto> captor = ArgumentCaptor.forClass(IngestionRequestDto.class);
        verify(ingestionService).process(captor.capture(), any());
        assertInstanceOf(MetricIngestionRequestDto.class, captor.getValue());
    }

    @Test
    void consumeIngestionMessage_shouldWrapException_whenPayloadInvalid() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("langa", 0, 0L, "key", "not-json");
        when(kafkaCredentialsMapper.mapFromKafkaHeaders(record)).thenReturn(credentials);

        assertThrows(KafkaIngestionException.class, () -> consumer.consumeIngestionMessage(record));
    }
}
