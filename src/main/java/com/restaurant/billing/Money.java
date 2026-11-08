package com.restaurant.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {
    private Money() {}

    public static BigDecimal of(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal of(String value) {
        return of(new BigDecimal(value));
    }

    public static BigDecimal percentOf(BigDecimal amount, BigDecimal percent) {
        return of(amount.multiply(percent).divide(BigDecimal.valueOf(100)));
    }
}
