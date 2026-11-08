package com.restaurant.billing;

import com.restaurant.RestaurantException;

import java.math.BigDecimal;

public class PercentageDiscount extends Discount {
    private final BigDecimal percent;

    public PercentageDiscount(BigDecimal percent) {
        if (percent.signum() < 0 || percent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new RestaurantException("Discount percent must be between 0 and 100");
        }
        this.percent = percent;
    }

    @Override
    protected BigDecimal calculate(BigDecimal subtotal) {
        return Money.percentOf(subtotal, percent);
    }

    @Override
    public String description() {
        return percent.stripTrailingZeros().toPlainString() + "% off";
    }
}
