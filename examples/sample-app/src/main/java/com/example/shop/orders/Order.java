package com.example.shop.orders;

import java.math.BigDecimal;
import java.time.Instant;

public record Order(long id, long customerId, String product, int quantity, BigDecimal amount, Instant createdAt) {
}
