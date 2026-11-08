package com.restaurant.model;

import com.restaurant.billing.Money;

import java.math.BigDecimal;

/** One line of an order. The unit price is captured when the item is first added. */
public class OrderItem {
    private final int menuItemId;
    private final String name;
    private final BigDecimal unitPrice;
    private int quantity;

    public OrderItem(int menuItemId, String name, BigDecimal unitPrice, int quantity) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.unitPrice = Money.of(unitPrice);
        this.quantity = quantity;
    }

    public int getMenuItemId() { return menuItemId; }
    public String getName() { return name; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }

    void increase(int qty) { quantity += qty; }
    void decrease(int qty) { quantity -= qty; }

    public BigDecimal lineTotal() {
        return Money.of(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }
}
