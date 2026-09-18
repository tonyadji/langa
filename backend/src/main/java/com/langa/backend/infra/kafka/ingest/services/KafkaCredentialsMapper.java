package com.langa.backend.infra.kafka.ingest.services;

import com.langa.backend.domain.applications.services.IngestionCredentials;
import com.langa.backend.domain.applications.services.IngestionSecurity;
import com.langa.backend.infra.kafka.ingest.dto.KafkaIngestionHeaders;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Service to convert Kafka message headers to IngestionCredentials
 */
@Service
@Slf4j
public class KafkaCredentialsMapper {

  public static final String X_AGENT_SIGNATURE = "xAgentSignature";

  public IngestionCredentials mapFromKafkaHeaders(ConsumerRecord<String, String> messageRecord) {
        String userAgent = getHeaderValue(messageRecord, "xUserAgent");
        String appKey = getHeaderValue(messageRecord, "xAppKey");
        String accountKey = getHeaderValue(messageRecord, "xAccountKey");
        String timestamp = getHeaderValue(messageRecord, "xTimestamp");
        String signature = getHeaderValue(messageRecord, X_AGENT_SIGNATURE);
        
        // Debug: display all received headers
        log.debug("Kafka headers received:");
        messageRecord.headers().forEach(header -> {
            String value = new String(header.value(), StandardCharsets.UTF_8);
            log.debug("  {}: '{}'", header.key(), value);
        });

        // Check if we have a separate nonce in the headers
        String nonce = getHeaderValue(messageRecord, "xNonce");
        
        // If the signature does not contain a nonce and we don't have a separate nonce header,
        // try to reconstruct the expected format
        if (signature != null && !signature.contains(":") && nonce != null) {
            signature = nonce + ":" + signature;
            log.debug("Reconstructed signature with nonce: '{}'", signature);
        }

        return new IngestionCredentials(
                userAgent,
                appKey,
                accountKey,
                timestamp,
                signature,
                IngestionSecurity.CredentialType.KAFKA
        );
    }

    public KafkaIngestionHeaders extractHeaders(ConsumerRecord<String, String> messageRecord) {
        KafkaIngestionHeaders headers = new KafkaIngestionHeaders();
        headers.setXUserAgent(getHeaderValue(messageRecord, "xUserAgent"));
        headers.setXAppKey(getHeaderValue(messageRecord, "xAppKey"));
        headers.setXAccountKey(getHeaderValue(messageRecord, "xAccountKey"));
        headers.setXTimestamp(getHeaderValue(messageRecord, "xTimestamp"));
        headers.setXAgentSignature(getHeaderValue(messageRecord, X_AGENT_SIGNATURE));
        return headers;
    }

    private String getHeaderValue(ConsumerRecord<String, String> messageRecord, String headerKey) {
        Header header = messageRecord.headers().lastHeader(headerKey);
        if (header != null && header.value() != null) {
            String value = new String(header.value(), StandardCharsets.UTF_8);
            
            // Debug logging to see header values
            if (X_AGENT_SIGNATURE.equals(headerKey)) {
                log.debug("Raw signature header value for {}: '{}'", headerKey, value);
                // Check if the signature already contains the nonce:signature format
                if (!value.contains(":") && value.length() > 10) {
                    log.warn("Signature header '{}' appears to be missing nonce part. Value: '{}'", headerKey, value);
                }
            }
            
            return value;
        }
        return null;
    }
}