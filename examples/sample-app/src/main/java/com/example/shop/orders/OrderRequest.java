package com.example.shop.orders;

import java.math.BigDecimal;

public record OrderRequest(long customerId, String product, int quantity, BigDecimal amount) {
}
