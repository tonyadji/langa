package com.example.shop.payments;

import com.capricedumardi.agent.core.metrics.Monitored;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/** Fake payment provider: sometimes slow, sometimes declining. */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Monitored(name = "payments.authorize")
    public void authorize(long orderId, long customerId, BigDecimal amount) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int latency = 30 + random.nextInt(120);
        if (latency > 130) {
            log.warn("Payment provider slow to answer for order {} ({} ms)", orderId, latency);
        }
        sleep(latency);

        if (random.nextInt(100) < 8) {
            log.error("Payment declined for order {}: insufficient funds (customer {}, amount {})",
                    orderId, customerId, amount);
            throw new PaymentDeclinedException("Payment declined for order " + orderId);
        }
        log.info("Payment authorized for order {} ({} EUR)", orderId, amount);
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
