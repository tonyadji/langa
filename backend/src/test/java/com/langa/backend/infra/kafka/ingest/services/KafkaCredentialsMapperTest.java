package com.langa.backend.infra.kafka.ingest.services;

import com.langa.backend.domain.applications.services.IngestionCredentials;
import com.langa.backend.domain.applications.services.IngestionSecurity;
import com.langa.backend.infra.kafka.ingest.dto.KafkaIngestionHeaders;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class KafkaCredentialsMapperTest {

    private final KafkaCredentialsMapper mapper = new KafkaCredentialsMapper();

    private ConsumerRecord<String, String> recordWithHeaders(String signature, String nonce) {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("langa", 0, 0L, "key", "value");
        record.headers().add(new RecordHeader("xUserAgent", "agent-v1".getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("xAppKey", "app-key".getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("xAccountKey", "acc-key".getBytes(StandardCharsets.UTF_8)));
        record.headers().add(new RecordHeader("xTimestamp", "12345".getBytes(StandardCharsets.UTF_8)));
        if (signature != null) {
            record.headers().add(new RecordHeader("xAgentSignature", signature.getBytes(StandardCharsets.UTF_8)));
        }
        if (nonce != null) {
            record.headers().add(new RecordHeader("xNonce", nonce.getBytes(StandardCharsets.UTF_8)));
        }
        return record;
    }

    @Test
    void mapFromKafkaHeaders_shouldExtractAllFields() {
        ConsumerRecord<String, String> record = recordWithHeaders("nonce-1:signature-hash", null);

        IngestionCredentials credentials = mapper.mapFromKafkaHeaders(record);

        assertEquals("agent-v1", credentials.userAgent());
        assertEquals("app-key", credentials.appKey());
        assertEquals("acc-key", credentials.accountKey());
        assertEquals("12345", credentials.timestamp());
        assertEquals("nonce-1:signature-hash", credentials.signature());
        assertEquals(IngestionSecurity.CredentialType.KAFKA, credentials.credentialType());
    }

    @Test
    void mapFromKafkaHeaders_shouldReconstructSignature_whenNonceIsSeparate() {
        ConsumerRecord<String, String> record = recordWithHeaders("signature-hash-only", "nonce-1");

        IngestionCredentials credentials = mapper.mapFromKafkaHeaders(record);

        assertEquals("nonce-1:signature-hash-only", credentials.signature());
    }

    @Test
    void mapFromKafkaHeaders_shouldReturnNull_whenHeaderMissing() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("langa", 0, 0L, "key", "value");

        IngestionCredentials credentials = mapper.mapFromKafkaHeaders(record);

        assertNull(credentials.userAgent());
        assertNull(credentials.signature());
    }

    @Test
    void extractHeaders_shouldPopulateDto() {
        ConsumerRecord<String, String> record = recordWithHeaders("nonce-1:signature-hash", null);

        KafkaIngestionHeaders headers = mapper.extractHeaders(record);

        assertEquals("agent-v1", headers.getXUserAgent());
        assertEquals("app-key", headers.getXAppKey());
        assertEquals("acc-key", headers.getXAccountKey());
        assertEquals("12345", headers.getXTimestamp());
        assertEquals("nonce-1:signature-hash", headers.getXAgentSignature());
    }
}
