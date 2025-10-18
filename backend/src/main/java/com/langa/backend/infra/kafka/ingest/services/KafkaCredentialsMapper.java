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
        
        // Debug: afficher tous les headers reçus
        log.debug("Kafka headers received:");
        messageRecord.headers().forEach(header -> {
            String value = new String(header.value(), StandardCharsets.UTF_8);
            log.debug("  {}: '{}'", header.key(), value);
        });

        // Vérifier si nous avons un nonce séparé dans les headers
        String nonce = getHeaderValue(messageRecord, "xNonce");
        
        // Si la signature ne contient pas de nonce et que nous n'avons pas de header nonce séparé,
        // essayer de reconstruire le format attendu
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
            
            // Debug logging pour voir les valeurs des headers
            if (X_AGENT_SIGNATURE.equals(headerKey)) {
                log.debug("Raw signature header value for {}: '{}'", headerKey, value);
                // Vérifier si la signature contient déjà le format nonce:signature
                if (!value.contains(":") && value.length() > 10) {
                    log.warn("Signature header '{}' appears to be missing nonce part. Value: '{}'", headerKey, value);
                }
            }
            
            return value;
        }
        return null;
    }
}