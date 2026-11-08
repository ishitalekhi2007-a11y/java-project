package com.restaurant.billing;

import java.math.BigDecimal;

/** Base type for charges levied on the discounted subtotal. */
public abstract class Tax {
    protected final BigDecimal ratePercent;

    protected Tax(BigDecimal ratePercent) {
        this.ratePercent = ratePercent;
    }

    public abstract String name();

    public BigDecimal amountOn(BigDecimal taxableAmount) {
        return Money.percentOf(taxableAmount, ratePercent);
    }

    public String label() {
        return name() + " " + ratePercent.stripTrailingZeros().toPlainString() + "%";
    }
}
