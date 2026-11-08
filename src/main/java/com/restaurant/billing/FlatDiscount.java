package com.restaurant.billing;

import com.restaurant.RestaurantException;

import java.math.BigDecimal;

/** A fixed amount off, applied only when the subtotal reaches a minimum. */
public class FlatDiscount extends Discount {
    private final BigDecimal amount;
    private final BigDecimal minimumSubtotal;

    public FlatDiscount(BigDecimal amount, BigDecimal minimumSubtotal) {
        if (amount.signum() < 0 || minimumSubtotal.signum() < 0) {
            throw new RestaurantException("Discount amount and minimum must not be negative");
        }
        this.amount = Money.of(amount);
        this.minimumSubtotal = Money.of(minimumSubtotal);
    }

    @Override
    protected BigDecimal calculate(BigDecimal subtotal) {
        return subtotal.compareTo(minimumSubtotal) >= 0 ? amount : BigDecimal.ZERO;
    }

    @Override
    public String description() {
        return amount + " off on orders of " + minimumSubtotal + " or more";
    }
}
