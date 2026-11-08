package com.restaurant.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Bill {
    private int id;
    private final int orderId;
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final String discountDescription;
    private final BigDecimal tax;
    private final String taxDescription;
    private final BigDecimal total;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private final LocalDateTime createdAt;
    private LocalDateTime paidAt;

    public Bill(int id, int orderId, BigDecimal subtotal, BigDecimal discount, String discountDescription,
                BigDecimal tax, String taxDescription, BigDecimal total, PaymentStatus paymentStatus,
                PaymentMethod paymentMethod, LocalDateTime createdAt, LocalDateTime paidAt) {
        this.id = id;
        this.orderId = orderId;
        this.subtotal = subtotal;
        this.discount = discount;
        this.discountDescription = discountDescription;
        this.tax = tax;
        this.taxDescription = taxDescription;
        this.total = total;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscount() { return discount; }
    public String getDiscountDescription() { return discountDescription; }
    public BigDecimal getTax() { return tax; }
    public String getTaxDescription() { return taxDescription; }
    public BigDecimal getTotal() { return total; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public boolean isPaid() { return paymentStatus == PaymentStatus.PAID; }

    @Override
    public String toString() {
        return String.format("Bill #%-3d | order #%-3d | total %9s | %s%s",
                id, orderId, total, paymentStatus, paymentMethod == null ? "" : " (" + paymentMethod + ")");
    }
}
