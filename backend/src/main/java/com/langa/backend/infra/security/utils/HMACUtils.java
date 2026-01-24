package com.langa.backend.infra.security.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HMACUtils {
    private static final Logger log = LogManager.getLogger(HMACUtils.class);

    private HMACUtils() {
    }

    public static String hash(String message, String secret) {
        try {
            Mac sha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256.init(secretKey);

            byte[] encodedHash = sha256.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encodedHash);
        } catch (Exception e) {
            log.error("Error hashing message: {}", e.getMessage());
        }
        return null;
    }
    public static String clean(String s) {return s.trim();}
}
