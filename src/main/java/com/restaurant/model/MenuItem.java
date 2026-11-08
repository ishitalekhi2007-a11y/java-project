package com.restaurant.model;

import com.restaurant.billing.Money;

import java.math.BigDecimal;

public class MenuItem {
    private int id;
    private String name;
    private Category category;
    private BigDecimal price;
    private boolean available;

    public MenuItem(int id, String name, Category category, BigDecimal price, boolean available) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = Money.of(price);
        this.available = available;
    }

    public MenuItem(String name, Category category, BigDecimal price) {
        this(0, name, category, price, true);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = Money.of(price); }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return String.format("#%-3d %-22s %-12s %8s  %s",
                id, name, category, price, available ? "" : "(unavailable)");
    }
}
