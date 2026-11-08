package com.restaurant.dao;

import com.restaurant.model.Bill;
import com.restaurant.model.PaymentMethod;
import com.restaurant.model.PaymentStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BillDao {

    public Bill create(Connection c, Bill bill) throws SQLException {
        String sql = "INSERT INTO bills (order_id, subtotal, discount, discount_desc, tax, tax_desc, total, "
                + "payment_status, payment_method, created_at, paid_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bill.getOrderId());
            ps.setBigDecimal(2, bill.getSubtotal());
            ps.setBigDecimal(3, bill.getDiscount());
            ps.setString(4, bill.getDiscountDescription());
            ps.setBigDecimal(5, bill.getTax());
            ps.setString(6, bill.getTaxDescription());
            ps.setBigDecimal(7, bill.getTotal());
            ps.setString(8, bill.getPaymentStatus().name());
            ps.setString(9, bill.getPaymentMethod() == null ? null : bill.getPaymentMethod().name());
            ps.setTimestamp(10, Timestamp.valueOf(bill.getCreatedAt()));
            ps.setTimestamp(11, bill.getPaidAt() == null ? null : Timestamp.valueOf(bill.getPaidAt()));
            ps.executeUpdate();
            bill.setId(Dao.generatedId(ps));
            return bill;
        }
    }

    public Optional<Bill> findById(Connection c, int id, boolean forUpdate) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM bills WHERE id = ?" + Dao.lock(forUpdate))) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Bill> findAll(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM bills ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            List<Bill> bills = new ArrayList<>();
            while (rs.next()) {
                bills.add(map(rs));
            }
            return bills;
        }
    }

    public void markPaid(Connection c, int billId, PaymentMethod method, LocalDateTime paidAt) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE bills SET payment_status = ?, payment_method = ?, paid_at = ? WHERE id = ?")) {
            ps.setString(1, PaymentStatus.PAID.name());
            ps.setString(2, method.name());
            ps.setTimestamp(3, Timestamp.valueOf(paidAt));
            ps.setInt(4, billId);
            ps.executeUpdate();
        }
    }

    private Bill map(ResultSet rs) throws SQLException {
        String method = rs.getString("payment_method");
        return new Bill(rs.getInt("id"), rs.getInt("order_id"), rs.getBigDecimal("subtotal"),
                rs.getBigDecimal("discount"), rs.getString("discount_desc"), rs.getBigDecimal("tax"),
                rs.getString("tax_desc"), rs.getBigDecimal("total"),
                PaymentStatus.valueOf(rs.getString("payment_status")),
                method == null ? null : PaymentMethod.valueOf(method),
                Dao.toDateTime(rs.getTimestamp("created_at")), Dao.toDateTime(rs.getTimestamp("paid_at")));
    }
}
