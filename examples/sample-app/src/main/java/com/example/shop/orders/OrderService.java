package com.example.shop.orders;

import com.capricedumardi.agent.core.metrics.Monitored;
import com.example.shop.payments.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory orders: enough to produce realistic logs and timings. */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final int MAX_ORDERS = 500;

    private final PaymentService paymentService;
    private final AtomicLong ids = new AtomicLong(1000);
    private final List<Order> orders = Collections.synchronizedList(new ArrayList<>());

    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Monitored(name = "orders.create")
    public Order createOrder(OrderRequest request) {
        if (request.quantity() <= 0) {
            log.warn("Rejected order of customer {}: invalid quantity {}", request.customerId(), request.quantity());
            throw new IllegalArgumentException("Quantity must be positive");
        }
        final long id = ids.incrementAndGet();
        paymentService.authorize(id, request.customerId(), request.amount());

        final Order order = new Order(id, request.customerId(), request.product(), request.quantity(),
                request.amount(), Instant.now());
        orders.add(order);
        if (orders.size() > MAX_ORDERS) {
            orders.removeFirst();
        }
        log.info("Order {} created for customer {}: {} x {}", id, request.customerId(), request.quantity(),
                request.product());
        return order;
    }

    @Monitored(name = "orders.list")
    public List<Order> listOrders() {
        synchronized (orders) {
            log.debug("Listing {} orders", orders.size());
            return List.copyOf(orders);
        }
    }
}
