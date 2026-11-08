package com.restaurant;

/** A business-rule violation that should be shown to the user (e.g. "table is occupied"). */
public class RestaurantException extends RuntimeException {
    public RestaurantException(String message) {
        super(message);
    }
}
