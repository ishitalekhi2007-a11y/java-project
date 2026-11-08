package com.restaurant.model;

import java.math.BigDecimal;

/** A single change to an order, kept on a stack so the latest change can be undone. */
public record OrderEdit(Type type, int menuItemId, String itemName, BigDecimal unitPrice, int quantity) {

    public enum Type { ADD, REMOVE }

    @Override
    public String toString() {
        return (type == Type.ADD ? "added " : "removed ") + quantity + " x " + itemName;
    }
}
