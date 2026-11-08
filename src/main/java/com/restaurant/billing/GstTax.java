package com.restaurant.billing;

import java.math.BigDecimal;

public class GstTax extends Tax {
    public GstTax(BigDecimal ratePercent) {
        super(ratePercent);
    }

    @Override
    public String name() {
        return "GST";
    }
}
