package com.restaurant.billing;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

public record BillBreakdown(BigDecimal subtotal, BigDecimal discount, String discountDescription,
                            Map<String, BigDecimal> taxes, BigDecimal total) {

    public BigDecimal taxTotal() {
        return Money.of(taxes.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public String taxDescription() {
        return taxes.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("; "));
    }
}
