package com.restaurant.model;

import com.restaurant.RestaurantException;
import com.restaurant.billing.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Order {
    private int id;
    private final int tableId;
    private OrderStatus status;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items = new ArrayList<>();

    public Order(int id, int tableId, OrderStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.tableId = tableId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTableId() { return tableId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public boolean isOpen() { return status == OrderStatus.OPEN; }

    public Optional<OrderItem> findLine(int menuItemId) {
        return items.stream().filter(i -> i.getMenuItemId() == menuItemId).findFirst();
    }

    /** Adds qty to the existing line for this item, or creates a new line. Returns the affected line. */
    public OrderItem addItem(int menuItemId, String name, BigDecimal unitPrice, int qty) {
        requirePositive(qty);
        Optional<OrderItem> existing = findLine(menuItemId);
        if (existing.isPresent()) {
            existing.get().increase(qty);
            return existing.get();
        }
        OrderItem line = new OrderItem(menuItemId, name, unitPrice, qty);
        items.add(line);
        return line;
    }

    /** Removes qty from a line; the line is dropped when it reaches zero. Returns the affected line. */
    public OrderItem removeItem(int menuItemId, int qty) {
        requirePositive(qty);
        OrderItem line = findLine(menuItemId)
                .orElseThrow(() -> new RestaurantException("Item #" + menuItemId + " is not on this order"));
        if (qty > line.getQuantity()) {
            throw new RestaurantException("Only " + line.getQuantity() + " x " + line.getName() + " on this order");
        }
        line.decrease(qty);
        if (line.getQuantity() == 0) {
            items.remove(line);
        }
        return line;
    }

    public BigDecimal subtotal() {
        return Money.of(items.stream().map(OrderItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static void requirePositive(int qty) {
        if (qty <= 0) {
            throw new RestaurantException("Quantity must be greater than zero");
        }
    }
}
