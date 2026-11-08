package com.restaurant.billing;

import com.restaurant.model.Order;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** subtotal -> discount -> taxes on the discounted amount -> total. */
public class BillCalculator {
    private final List<Tax> taxes;

    public BillCalculator(List<Tax> taxes) {
        this.taxes = List.copyOf(taxes);
    }

    public BillBreakdown calculate(Order order, Discount discount) {
        BigDecimal subtotal = order.subtotal();
        BigDecimal discountAmount = discount.amountOn(subtotal);
        BigDecimal taxable = subtotal.subtract(discountAmount);

        Map<String, BigDecimal> taxLines = new LinkedHashMap<>();
        BigDecimal total = taxable;
        for (Tax tax : taxes) {
            BigDecimal amount = tax.amountOn(taxable);
            taxLines.put(tax.label(), amount);
            total = total.add(amount);
        }
        return new BillBreakdown(subtotal, discountAmount, discount.description(), taxLines, Money.of(total));
    }
}
