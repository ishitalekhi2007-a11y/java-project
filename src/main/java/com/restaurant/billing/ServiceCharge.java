package com.restaurant.billing;

import java.math.BigDecimal;

/** Service charge, waived on small bills below the threshold. */
public class ServiceCharge extends Tax {
    private final BigDecimal waivedBelow;

    public ServiceCharge(BigDecimal ratePercent, BigDecimal waivedBelow) {
        super(ratePercent);
        this.waivedBelow = waivedBelow;
    }

    @Override
    public String name() {
        return "Service charge";
    }

    @Override
    public BigDecimal amountOn(BigDecimal taxableAmount) {
        return taxableAmount.compareTo(waivedBelow) < 0 ? Money.of(BigDecimal.ZERO) : super.amountOn(taxableAmount);
    }
}
