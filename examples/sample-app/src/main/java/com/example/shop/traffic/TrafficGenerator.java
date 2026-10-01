package com.example.shop.traffic;

import com.example.shop.orders.OrderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulated customers calling the shop over HTTP, so that the dashboard shows live logs and metrics
 * (with their URI, method and status) without any manual action.
 */
@Component
@ConditionalOnProperty(prefix = "sample.traffic", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TrafficGenerator {

    private static final Logger log = LoggerFactory.getLogger(TrafficGenerator.class);
    private static final String[] PRODUCTS = {"keyboard", "mouse", "monitor", "laptop-stand", "usb-c-hub"};

    private final RestClient shop;

    public TrafficGenerator(@Value("${server.port}") int port) {
        this.shop = RestClient.create("http://localhost:" + port);
    }

    @Scheduled(initialDelayString = "5000", fixedDelayString = "${sample.traffic.interval-millis}")
    public void simulateCustomers() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        try {
            if (random.nextInt(100) < 70) {
                // A few orders with an invalid quantity, to produce warnings and 400s
                final int quantity = random.nextInt(100) < 5 ? 0 : 1 + random.nextInt(3);
                final BigDecimal amount = BigDecimal.valueOf(10 + random.nextDouble(290)).setScale(2, RoundingMode.HALF_UP);
                shop.post().uri("/orders")
                        .body(new OrderRequest(1 + random.nextInt(50), PRODUCTS[random.nextInt(PRODUCTS.length)], quantity, amount))
                        .retrieve()
                        .toBodilessEntity();
            } else {
                shop.get().uri("/orders").retrieve().toBodilessEntity();
            }
        } catch (RestClientResponseException e) {
            log.debug("Simulated call answered {}", e.getStatusCode());
        } catch (Exception e) {
            log.warn("Simulated call failed: {}", e.getMessage());
        }
    }
}
