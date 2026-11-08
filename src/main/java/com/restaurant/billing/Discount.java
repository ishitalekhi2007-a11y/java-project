package com.restaurant.billing;

import java.math.BigDecimal;

/** Base type for all discounts; BillCalculator works only against this abstraction. */
public abstract class Discount {

    /** The discount amount for the given subtotal, before capping. */
    protected abstract BigDecimal calculate(BigDecimal subtotal);

    public abstract String description();

    /** Never discounts more than the subtotal. */
    public final BigDecimal amountOn(BigDecimal subtotal) {
        return Money.of(calculate(subtotal).max(BigDecimal.ZERO).min(subtotal));
    }
}
