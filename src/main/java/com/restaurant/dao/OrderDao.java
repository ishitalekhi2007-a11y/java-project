package com.restaurant.dao;

import com.restaurant.model.Order;
import com.restaurant.model.OrderItem;
import com.restaurant.model.OrderStatus;

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

public class OrderDao {

    public Order create(Connection c, int tableId) throws SQLException {
        Order order = new Order(0, tableId, OrderStatus.OPEN, LocalDateTime.now());
        String sql = "INSERT INTO orders (table_id, status, created_at) VALUES (?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tableId);
            ps.setString(2, order.getStatus().name());
            ps.setTimestamp(3, Timestamp.valueOf(order.getCreatedAt()));
            ps.executeUpdate();
            order.setId(Dao.generatedId(ps));
            return order;
        }
    }

    /** Loads the order with its items. */
    public Optional<Order> findById(Connection c, int id, boolean forUpdate) throws SQLException {
        Order order;
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM orders WHERE id = ?" + Dao.lock(forUpdate))) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                order = map(rs);
            }
        }
        loadItems(c, order);
        return Optional.of(order);
    }

    public List<Order> findByStatus(Connection c, OrderStatus status) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM orders WHERE status = ? ORDER BY id")) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(map(rs));
                }
            }
        }
        for (Order order : orders) {
            loadItems(c, order);
        }
        return orders;
    }

    /** Persists a line's current quantity; a zero quantity deletes the line. */
    public void saveLine(Connection c, int orderId, OrderItem line) throws SQLException {
        if (line.getQuantity() == 0) {
            try (PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM order_items WHERE order_id = ? AND menu_item_id = ?")) {
                ps.setInt(1, orderId);
                ps.setInt(2, line.getMenuItemId());
                ps.executeUpdate();
            }
            return;
        }
        try (PreparedStatement ps = c.prepareStatement(
                "MERGE INTO order_items (order_id, menu_item_id, quantity, unit_price) KEY (order_id, menu_item_id) "
                        + "VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, orderId);
            ps.setInt(2, line.getMenuItemId());
            ps.setInt(3, line.getQuantity());
            ps.setBigDecimal(4, line.getUnitPrice());
            ps.executeUpdate();
        }
    }

    public void updateStatus(Connection c, int orderId, OrderStatus status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE orders SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    private void loadItems(Connection c, Order order) throws SQLException {
        String sql = "SELECT oi.menu_item_id, m.name, oi.unit_price, oi.quantity FROM order_items oi "
                + "JOIN menu_items m ON m.id = oi.menu_item_id WHERE oi.order_id = ? ORDER BY m.name";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, order.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    order.addItem(rs.getInt("menu_item_id"), rs.getString("name"),
                            rs.getBigDecimal("unit_price"), rs.getInt("quantity"));
                }
            }
        }
    }

    private Order map(ResultSet rs) throws SQLException {
        return new Order(rs.getInt("id"), rs.getInt("table_id"), OrderStatus.valueOf(rs.getString("status")),
                Dao.toDateTime(rs.getTimestamp("created_at")));
    }
}
