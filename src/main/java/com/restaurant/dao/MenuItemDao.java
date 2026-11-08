package com.restaurant.dao;

import com.restaurant.model.Category;
import com.restaurant.model.MenuItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MenuItemDao {

    public MenuItem create(Connection c, MenuItem item) throws SQLException {
        String sql = "INSERT INTO menu_items (name, category, price, available) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory().name());
            ps.setBigDecimal(3, item.getPrice());
            ps.setBoolean(4, item.isAvailable());
            ps.executeUpdate();
            item.setId(Dao.generatedId(ps));
            return item;
        }
    }

    public List<MenuItem> findAll(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM menu_items ORDER BY category, name");
             ResultSet rs = ps.executeQuery()) {
            List<MenuItem> items = new ArrayList<>();
            while (rs.next()) {
                items.add(map(rs));
            }
            return items;
        }
    }

    public Optional<MenuItem> findById(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM menu_items WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public void update(Connection c, MenuItem item) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE menu_items SET name = ?, category = ?, price = ?, available = ? WHERE id = ?")) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory().name());
            ps.setBigDecimal(3, item.getPrice());
            ps.setBoolean(4, item.isAvailable());
            ps.setInt(5, item.getId());
            ps.executeUpdate();
        }
    }

    public boolean delete(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM menu_items WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private MenuItem map(ResultSet rs) throws SQLException {
        return new MenuItem(rs.getInt("id"), rs.getString("name"), Category.valueOf(rs.getString("category")),
                rs.getBigDecimal("price"), rs.getBoolean("available"));
    }
}
