package com.restaurant;

import com.restaurant.billing.BillBreakdown;
import com.restaurant.billing.BillCalculator;
import com.restaurant.billing.FlatDiscount;
import com.restaurant.billing.GstTax;
import com.restaurant.billing.NoDiscount;
import com.restaurant.billing.PercentageDiscount;
import com.restaurant.billing.ServiceCharge;
import com.restaurant.model.Order;
import com.restaurant.model.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BillCalculatorTest {
    private final BillCalculator calculator = new BillCalculator(List.of(
            new GstTax(new BigDecimal("5")),
            new ServiceCharge(new BigDecimal("10"), new BigDecimal("500"))));

    private static Order orderWorth(String... prices) {
        Order order = new Order(1, 1, OrderStatus.OPEN, LocalDateTime.now());
        for (int i = 0; i < prices.length; i++) {
            order.addItem(i + 1, "Item " + i, new BigDecimal(prices[i]), 1);
        }
        return order;
    }

    @Test
    void percentageDiscountThenTaxesOnDiscountedAmount() {
        BillBreakdown b = calculator.calculate(orderWorth("400", "200"), new PercentageDiscount(new BigDecimal("10")));
        assertEquals(new BigDecimal("600.00"), b.subtotal());
        assertEquals(new BigDecimal("60.00"), b.discount());
        assertEquals(new BigDecimal("27.00"), b.taxes().get("GST 5%"));
        assertEquals(new BigDecimal("54.00"), b.taxes().get("Service charge 10%"));
        assertEquals(new BigDecimal("621.00"), b.total());
    }

    @Test
    void serviceChargeWaivedBelowThreshold() {
        BillBreakdown b = calculator.calculate(orderWorth("300"), new NoDiscount());
        assertEquals(new BigDecimal("0.00"), b.taxes().get("Service charge 10%"));
        assertEquals(new BigDecimal("315.00"), b.total());
    }

    @Test
    void flatDiscountNeedsMinimumAndIsCappedAtSubtotal() {
        FlatDiscount hundredOffAbove500 = new FlatDiscount(new BigDecimal("100"), new BigDecimal("500"));
        assertEquals(new BigDecimal("0.00"), calculator.calculate(orderWorth("300"), hundredOffAbove500).discount());

        FlatDiscount huge = new FlatDiscount(new BigDecimal("1000"), BigDecimal.ZERO);
        BillBreakdown b = calculator.calculate(orderWorth("300"), huge);
        assertEquals(new BigDecimal("300.00"), b.discount());
        assertEquals(new BigDecimal("0.00"), b.total());
    }

    @Test
    void invalidPercentageRejected() {
        assertThrows(RestaurantException.class, () -> new PercentageDiscount(new BigDecimal("120")));
    }
}
