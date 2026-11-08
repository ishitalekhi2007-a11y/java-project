package com.restaurant.billing;

import java.math.BigDecimal;

public class NoDiscount extends Discount {
    @Override
    protected BigDecimal calculate(BigDecimal subtotal) {
        return BigDecimal.ZERO;
    }

    @Override
    public String description() {
        return "No discount";
    }
}
